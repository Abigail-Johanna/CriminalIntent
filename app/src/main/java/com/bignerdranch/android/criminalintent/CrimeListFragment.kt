package com.bignerdranch.android.criminalintent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val VIEW_TYPE_NORMAL = 0
private const val VIEW_TYPE_POLICE = 1
private const val REQUEST_CRIME = 1

class CrimeListFragment : Fragment() {

    private lateinit var crimeRecyclerView: RecyclerView
    private var adapter: CrimeAdapter? = null
    private var lastClickedPosition: Int = -1

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this)[CrimeListViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)

        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view)
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        
        updateUI()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimes.observe(viewLifecycleOwner) { crimes ->
            crimes?.let {
                updateUI(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == REQUEST_CRIME) {
            // Handle result if needed
        }
    }

    private fun updateUI(crimes: List<Crime>? = null) {
        val crimeList = crimes ?: CrimeLab.get(requireContext()).getCrimes()
        
        if (adapter == null) {
            adapter = CrimeAdapter(crimeList)
            crimeRecyclerView.adapter = adapter
        } else {
            adapter?.crimes = crimeList
            if (lastClickedPosition != -1) {
                adapter?.notifyItemChanged(lastClickedPosition)
                lastClickedPosition = -1
            } else {
                adapter?.notifyDataSetChanged()
            }
        }
    }

    private open inner class CrimeHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)
        private lateinit var crime: Crime

        init {
            itemView.setOnClickListener(this)
        }

        open fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = this.crime.title
            dateTextView.text = this.crime.date.toString()
            solvedImageView.visibility = if (crime.isSolved) View.VISIBLE else View.GONE
        }

        override fun onClick(v: View) {
            lastClickedPosition = adapterPosition
            val intent = CrimePagerActivity.newIntent(requireContext(), crime.id)
            startActivityForResult(intent, REQUEST_CRIME)
        }
    }

    private inner class PoliceCrimeHolder(view: View) : CrimeHolder(view) {
        private val contactPoliceButton: Button = itemView.findViewById(R.id.contact_police_button)

        override fun bind(crime: Crime) {
            super.bind(crime)
            contactPoliceButton.setOnClickListener {
                Toast.makeText(context, "Police contacted for ${crime.title}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private inner class CrimeAdapter(var crimes: List<Crime>) : RecyclerView.Adapter<CrimeHolder>() {

        override fun getItemViewType(position: Int): Int {
            return if (crimes[position].requiresPolice) VIEW_TYPE_POLICE else VIEW_TYPE_NORMAL
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val view = if (viewType == VIEW_TYPE_POLICE) {
                layoutInflater.inflate(R.layout.list_item_crime_police, parent, false)
            } else {
                layoutInflater.inflate(R.layout.list_item_crime, parent, false)
            }
            return if (viewType == VIEW_TYPE_POLICE) {
                PoliceCrimeHolder(view)
            } else {
                CrimeHolder(view)
            }
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            holder.bind(crimes[position])
        }

        override fun getItemCount() = crimes.size
    }

    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }
}