package com.muratozcan.todolist.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.muratozcan.todolist.R
import com.muratozcan.todolist.controller.OnTaskClickListener
import com.muratozcan.todolist.databinding.ItemRowBinding
import com.muratozcan.todolist.model.Task
import com.muratozcan.todolist.roomdb.TaskDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.CompletableObserver
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


class ListAdapter(
    private val taskList: List<Task>,
) :
    RecyclerView.Adapter<ListAdapter.ViewHolder>() {



    private val compositeDisposable = CompositeDisposable()

    class ViewHolder(val binding : ItemRowBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val db = Room.databaseBuilder(holder.binding.checkBox.context, TaskDatabase::class.java, "Tasks").build()
        val taskDao = db.taskDao()

        holder.binding.checkBox.text = taskList[position].name
        holder.binding.checkBox.isChecked = taskList[position].isCompleted

        val remainingDay = convertToDate(taskList[position].dueDate, LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/M/yyyy")).toString())


        if(remainingDay.toInt() == 0){
            if(taskList[position].isCompleted){
                holder.binding.remainingText.text = "Completed"
                holder.binding.frameLayout.setBackgroundColor(Color.rgb(76,175,80))
            } else {
                holder.binding.remainingText.text = "End day"
                holder.binding.frameLayout.setBackgroundColor(Color.rgb(255, 235, 59))
            }
        }
        else if(remainingDay.toInt() > 0){
            holder.binding.frameLayout.setBackgroundColor(Color.rgb(76, 175, 80))
            if(taskList[position].isCompleted){
                holder.binding.remainingText.text = "Completed"
            } else {
                holder.binding.remainingText.text = "Remaining $remainingDay days"
            }
        }
        else{
            if(taskList[position].isCompleted){
                holder.binding.remainingText.text = "Completed"
                holder.binding.frameLayout.setBackgroundColor(Color.rgb(76,175,80))
            } else{
                holder.binding.remainingText.text = "${kotlin.math.abs(remainingDay.toInt())} days late"
                holder.binding.frameLayout.setBackgroundColor(Color.rgb(255,0,0))
            }

        }

        holder.itemView.setOnClickListener {
            val text = taskList[position].name
            val duration = Toast.LENGTH_SHORT
            val toast = Toast.makeText(holder.itemView.context, text, duration)
            toast.show()

            var bundle = Bundle()
            bundle.putString("name", taskList[position].name)
            bundle.putString("notes", taskList[position].notes)
            bundle.putString("dueDate", taskList[position].dueDate)
            bundle.putSerializable("task", taskList[position])

            holder.itemView.findNavController().navigate(R.id.SecondFragment, bundle)
        }

        holder.binding.checkBox.setOnCheckedChangeListener{
                _, _ ->
            if(!taskList[position].isCompleted) {

                compositeDisposable.add(
                    taskDao.updateCompleted(true, taskList[position].id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableObserver<String>(), CompletableObserver {
                        override fun onComplete() {
                            holder.itemView.findNavController().navigate(R.id.FirstFragment)
                        }
                        override fun onError(e: Throwable) {
                        }

                        override fun onNext(value: String) {
                        }
                    })
                )

            } else {
                compositeDisposable.add(
                     taskDao.updateCompleted(false, taskList[position].id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<String>(), CompletableObserver {
                            override fun onComplete() {
                                holder.itemView.findNavController().navigate(R.id.FirstFragment)
                            }
                            override fun onError(e: Throwable) {
                            }

                            override fun onNext(value: String) {
                            }
                        })
                )
            }

        }
    }

    private fun convertToDate(currentDate: String, date: String) : String{
        val dateFormatter: DateTimeFormatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern("dd/M/yyyy")
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val from = LocalDate.parse(date, dateFormatter)
        val to = LocalDate.parse(currentDate, dateFormatter)

        return ChronoUnit.DAYS.between(from, to).toString()
    }

}
