package com.muratozcan.todolist.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.muratozcan.todolist.R
import com.muratozcan.todolist.databinding.FragmentFirstBinding
import com.muratozcan.todolist.model.Task
import com.muratozcan.todolist.adapter.ListAdapter
import com.muratozcan.todolist.roomdb.TaskDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


class FirstFragment : Fragment(){

    private var _binding: FragmentFirstBinding? = null
    private lateinit var taskList : ArrayList<Task>
    private val compositeDisposable = CompositeDisposable()

    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        taskList = ArrayList<Task>()

        binding.recyclerView.layoutManager = LinearLayoutManager(view?.context)
        val taskAdapter = ListAdapter(taskList)
        binding.recyclerView.adapter = taskAdapter



        val db = Room.databaseBuilder(requireActivity().applicationContext, TaskDatabase::class.java, "Tasks").build()
        val taskDao = db.taskDao()

        compositeDisposable.add(
            taskDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )

        return binding.root
    }

    private fun handleResponse(taskList : List<Task>) {
        binding.recyclerView.layoutManager = LinearLayoutManager(view?.context)
        val adapter = ListAdapter(taskList)
        binding.recyclerView.adapter = adapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}