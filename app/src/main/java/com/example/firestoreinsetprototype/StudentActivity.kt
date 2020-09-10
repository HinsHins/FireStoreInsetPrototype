package com.example.firestoreinsetprototype

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.firestoreinsetprototype.Adaptor.StudentAdaptor
import com.example.firestoreinsetprototype.Model.Student
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_student.*

class StudentActivity : AppCompatActivity() {
    private val students = ArrayList<Student>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student)

        student_list_view.adapter = StudentAdaptor(this,R.layout.student_listview_item,students)


        var fb = FirebaseFirestore.getInstance()

        student_insert.setOnClickListener {
            var id = sid_et.text.toString().trim()
            var name = sname_et.text.toString().trim()
            var email = semail_et.text.toString().trim()
            val programme = sprogramme_et.text.toString().trim()
            val country = scountry_et.text.toString().trim()

            if (id != "" && name != "" && email != "" && programme != "" && country != "") {
                var student = Student(id, name, email, programme, country)
                Log.d("Student", "$student")
                writeStudent(student)
                Toast.makeText(this@StudentActivity, "Insert successful", Toast.LENGTH_SHORT)
                    .show()
            } else
                Toast.makeText(
                    this@StudentActivity,
                    "Please fill all fields before insert",
                    Toast.LENGTH_SHORT
                ).show()
        }

        val studentRef = fb.collection("students")
        studentRef.get()
            .addOnSuccessListener { result->
                for(document in result){
                    Log.d("Student", "${document.id} => ${document.data}")
                    var student = document.toObject(Student::class.java)
                    Log.d("Student","$student")
                    students.add(student)
                }
                Log.d("load Student", "$students")
                (student_list_view.adapter as StudentAdaptor).notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.d("", "Error getting documents: ", exception)
            }
    }

    private fun writeStudent(student: Student) {
        val studentRef = FirebaseFirestore.getInstance().collection("students")
        FirebaseFirestore.getInstance().collection("students")
            .document(student.id)
            .set(student)
            .addOnSuccessListener {
                Log.d("", "Student successfully written!")
                realTimeUpdate(studentRef)
            }
            .addOnFailureListener {e->
                Log.w("", "Error writing document", e)
            }
    }

    private fun realTimeUpdate(studentRef:CollectionReference){
        studentRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Fail", "Listen failed.", e)
                return@addSnapshotListener
            }

            val source = if (snapshot != null && snapshot.metadata.hasPendingWrites())
                "Local"
            else
                "Server"

            if (snapshot != null) {
                for (dc in snapshot.documentChanges) {
                    if (DocumentChange.Type.ADDED == dc.type) {
                        var s = dc.document.toObject(Student::class.java)
                        
                        for (student in students) {
                            if (student.id != s.id)
                                hasStudent = false
                        }
                        if (!hasStudent){
                            students.add(s)
                        Log.d("adding student", "${s.toString()}")
                        }
                    }
                }
                Log.d("RealTimeUpdate", "$students")
                (student_list_view.adapter as StudentAdaptor).notifyDataSetChanged()
            } else {
                android.util.Log.d("null", "$source data: null")
            }
        }

    }
}