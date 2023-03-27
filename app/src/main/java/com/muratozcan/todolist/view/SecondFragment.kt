package com.muratozcan.todolist.view

import android.content.Intent
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.muratozcan.todolist.databinding.FragmentSecondBinding
import com.muratozcan.todolist.model.Task
import com.muratozcan.todolist.roomdb.TaskDao
import com.muratozcan.todolist.roomdb.TaskDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private lateinit var db : TaskDatabase
    private lateinit var taskDao : TaskDao
    private val compositeDisposable = CompositeDisposable()
    private val binding get() = _binding!!
    private var name = ""
    private var notes = ""
    private lateinit var task: Task


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)

        db = Room.databaseBuilder(requireActivity().applicationContext, TaskDatabase::class.java, "Tasks").build()

        taskDao = db.taskDao()

        return binding.root

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {

            if(!it.getString("name").isNullOrBlank()){
                name = it.getString("name")!!
                notes = it.getString("notes")!!
                task = it.getSerializable("task") as Task

                binding.taskNameText.setText(name)
                binding.notesText.setText(notes)
                println(it.getString("dueDate"))

                binding.taskNameText.keyListener = null
                binding.notesText.keyListener = null
                binding.button.text = "DELETE"
                //binding.calendarView.visibility = View.INVISIBLE
            }
        }

        var calendarView = binding.calendarView
        calendarView.minDate = calendarView.date
        var currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/M/yyyy")).toString()

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            var day = dayOfMonth.toString()
            if(dayOfMonth / 10 == 0) {
                day = "0$dayOfMonth"
            }
            currentDate = (day + "/"
                    + (month + 1) + "/" + year)
        }

        binding.button.setOnClickListener{
            if(name.isNullOrBlank()){
                task = Task(binding.taskNameText.text.toString(), binding.notesText.text.toString(), false, currentDate)
                compositeDisposable.add(
                    taskDao.insert(task)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponse)
                )
            } else {
                task?.let {
                    compositeDisposable.add(
                        taskDao.delete(it)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::handleResponse)
                    )
                }
            }
        }
    }

    private fun handleResponse() {
        val intent = Intent (activity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        activity?.startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        compositeDisposable.clear()
    }
}