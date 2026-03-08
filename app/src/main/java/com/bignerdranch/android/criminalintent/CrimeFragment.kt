package com.bignerdranch.android.criminalintent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.bignerdranch.android.criminalintent.databinding.FragmentCrimeBinding
import java.util.Date
import java.util.UUID

private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 1

class CrimeFragment : Fragment() {

    private var _binding: FragmentCrimeBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private lateinit var crime: Crime

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val crimeId = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crime = CrimeLab.get(requireContext()).getCrime(crimeId) ?: Crime()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.crimeTitle.setText(crime.title)
        binding.crimeTitle.doOnTextChanged { text, _, _, _ ->
            crime.title = text.toString()
        }

        updateDate()
        binding.crimeDate.setOnClickListener {
            if (resources.getBoolean(R.bool.is_tablet)) {
                val dialog = DatePickerFragment.newInstance(crime.date)
                dialog.setTargetFragment(this, REQUEST_DATE)
                dialog.show(parentFragmentManager, DIALOG_DATE)
            } else {
                val intent = DatePickerActivity.newIntent(requireContext(), crime.date)
                startActivityForResult(intent, REQUEST_DATE)
            }
        }

        binding.crimeTime.setOnClickListener {
            val dialog = TimePickerFragment.newInstance(crime.date)
            dialog.setTargetFragment(this, REQUEST_TIME)
            dialog.show(parentFragmentManager, DIALOG_TIME)
        }

        binding.crimeSolved.apply {
            isChecked = crime.isSolved
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            REQUEST_DATE -> {
                val date = data?.getSerializableExtra(DatePickerFragment.EXTRA_DATE) as Date
                crime.date = date
                updateDate()
            }
            REQUEST_TIME -> {
                val date = data?.getSerializableExtra(TimePickerFragment.EXTRA_TIME) as Date
                crime.date = date
                updateDate()
            }
        }
    }

    private fun updateDate() {
        binding.crimeDate.text = crime.date.toString()
        binding.crimeTime.text = android.text.format.DateFormat.getTimeFormat(requireContext()).format(crime.date)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }
}
