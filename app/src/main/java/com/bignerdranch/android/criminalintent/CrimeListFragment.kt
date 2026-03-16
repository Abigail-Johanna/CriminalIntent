package com.bignerdranch.android.criminalintent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val VIEW_TYPE_NORMAL = 0
private const val VIEW_TYPE_POLICE = 1
private const val REQUEST_CRIME = 1
private const val SAVED_SUBTITLE_VISIBLE = "subtitle"
private const val MAX_CRIMES = 10

class CrimeListFragment : Fragment() {

    private lateinit var crimeRecyclerView: RecyclerView
    private lateinit var emptyView: View
    private lateinit var addCrimeButton: Button
    private var adapter: CrimeAdapter? = null
    private var lastClickedPosition: Int = -1
    private var isSubtitleVisible: Boolean = false

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this)[CrimeListViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)

        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view)
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        
        emptyView = view.findViewById(R.id.empty_view)
        addCrimeButton = view.findViewById(R.id.add_crime_button)
        addCrimeButton.setOnClickListener {
            createNewCrime()
        }

        if (savedInstanceState != null) {
            isSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE)
        }
        
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
        crimeListViewModel.loadCrimes(requireContext())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, isSubtitleVisible)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)

        val subtitleItem = menu.findItem(R.id.show_subtitle)
        if (isSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle)
        } else {
            subtitleItem.setTitle(R.string.show_subtitle)
        }

        val newCrimeItem = menu.findItem(R.id.new_crime)
        val crimeCount = crimeListViewModel.crimes.value?.size ?: 0
        newCrimeItem.isVisible = crimeCount < MAX_CRIMES
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_crime -> {
                createNewCrime()
                true
            }
            R.id.show_subtitle -> {
                isSubtitleVisible = !isSubtitleVisible
                activity?.invalidateOptionsMenu()
                updateSubtitle()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun createNewCrime() {
        val crimeCount = crimeListViewModel.crimes.value?.size ?: 0
        if (crimeCount >= MAX_CRIMES) {
            return
        }
        val crime = Crime()
        CrimeLab.get(requireContext()).addCrime(crime)
        val intent = CrimePagerActivity.newIntent(requireContext(), crime.id)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == REQUEST_CRIME) {
            // Handle result if needed
        }
    }

    private fun updateSubtitle() {
        val crimeCount = crimeListViewModel.crimes.value?.size ?: 0
        var subtitle: String? = resources.getQuantityString(R.plurals.subtitle_plural, crimeCount, crimeCount)

        if (!isSubtitleVisible) {
            subtitle = null
        }

        val activity = activity as AppCompatActivity
        activity.supportActionBar?.subtitle = subtitle
    }

    private fun updateUI(crimes: List<Crime>) {
        if (crimes.isEmpty()) {
            crimeRecyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            crimeRecyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }

        if (adapter == null) {
            adapter = CrimeAdapter(crimes)
            crimeRecyclerView.adapter = adapter
        } else {
            adapter?.crimes = crimes
            adapter?.notifyDataSetChanged()
        }

        activity?.invalidateOptionsMenu()
        updateSubtitle()
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

            val color = if (crime.isSolved) {
                ContextCompat.getColor(itemView.context, R.color.green)
            } else {
                ContextCompat.getColor(itemView.context, R.color.black)
            }
            titleTextView.setTextColor(color)
            dateTextView.setTextColor(color)
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
