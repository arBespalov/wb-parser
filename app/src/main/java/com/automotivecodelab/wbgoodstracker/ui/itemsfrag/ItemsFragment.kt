package com.automotivecodelab.wbgoodstracker.ui.itemsfrag

import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat.getDrawable
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.automotivecodelab.wbgoodstracker.*
import com.automotivecodelab.wbgoodstracker.databinding.ItemsFragmentBinding
import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import com.automotivecodelab.wbgoodstracker.ui.EventObserver
import com.automotivecodelab.wbgoodstracker.ui.MainActivity
import com.automotivecodelab.wbgoodstracker.ui.SignOutSnackbar
import com.automotivecodelab.wbgoodstracker.ui.itemsfrag.recyclerview.ItemsAdapter
import com.automotivecodelab.wbgoodstracker.ui.itemsfrag.recyclerview.MyItemDetailsLookup
import com.automotivecodelab.wbgoodstracker.ui.itemsfrag.recyclerview.MyItemKeyProvider
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialSharedAxis

class ItemsFragment : Fragment() {

    private val viewModel: ItemsViewModel by viewModels {
        ItemsViewModelFactory(getItemsRepository(), getUserRepository())
    }

    // references to views
    private var viewDataBinding: ItemsFragmentBinding? = null
    private var tracker: SelectionTracker<String>? = null
    private var mItemKeyProvider: MyItemKeyProvider? = null
    private var itemTouchHelper: ItemTouchHelper? = null
    private var actionMode: ActionMode? = null

    private var actionModeRestored = false
    private var closeActionModeLater = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.items_fragment, container, false)

        viewDataBinding = ItemsFragmentBinding.bind(view).apply {
            lifecycleOwner = viewLifecycleOwner
            viewmodel = viewModel
        }
        return view
    }

    override fun onDestroyView() {
        viewDataBinding = null
        tracker = null
        mItemKeyProvider = null
        itemTouchHelper = null
        actionMode = null
        super.onDestroyView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val intentValue = (requireActivity() as MainActivity).intentValue
        if (intentValue != null) {
            (requireActivity() as MainActivity).intentValue = null
            viewModel.addItem(intentValue)
        }

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        viewDataBinding?.toolbar?.setupWithNavController(navController, appBarConfiguration)

        setupRecycler()
        setupSpinner()
        setupOptionsMenu()
        setupSearchView()
        setupNavigation()

        viewDataBinding?.fabAdditem?.setOnClickListener { viewModel.addItem(null) }
        viewDataBinding?.swipeRefresh?.setOnRefreshListener { viewModel.updateItems() }

        viewModel.items.observe(viewLifecycleOwner) { items: List<Item>? ->
            if (items != null) {
                (viewDataBinding?.recyclerViewItems?.adapter as ItemsAdapter).replaceAll(items)
                mItemKeyProvider?.sortedListItems =
                    (viewDataBinding!!.recyclerViewItems.adapter as ItemsAdapter).sortedList
                mItemKeyProvider?.items = items
            }

            if (!viewModel.cachedSearchQuery.isNullOrEmpty()) {
                (
                    viewDataBinding?.toolbar?.menu?.findItem(R.id.menu_search)?.actionView as
                        SearchView
                    ).apply {
                    isIconified = false
                    setQuery(viewModel.cachedSearchQuery, false)
                }
                findItems(viewModel.cachedSearchQuery)
            }

            if (closeActionModeLater) { // called when user choose group in group picker
                closeActionModeLater = false
                actionMode?.finish()
            }

            if (savedInstanceState != null && !actionModeRestored) {
                actionModeRestored = true
                tracker?.onRestoreInstanceState(savedInstanceState)
                if (tracker!!.hasSelection() && actionMode == null) {
                    startActionMode()
                }
            }
        }

        viewModel.dataLoading.observe(
            viewLifecycleOwner,
            Observer {
                viewDataBinding?.swipeRefresh?.isRefreshing = it
            }
        )

        viewModel.authorizationErrorEvent.observe(
            viewLifecycleOwner,
            EventObserver {
                SignOutSnackbar().invoke(requireView()) { viewModel.signOut() }
            }
        )

        postponeEnterTransition()
        view?.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onResume() {
        // bug: onQueryTextListener called when navigating back to this fragment.
        // When setting listener in onResume, it works fine
        (viewDataBinding?.toolbar?.menu?.findItem(R.id.menu_search)?.actionView as SearchView)
            .setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?) = false

                override fun onQueryTextChange(newText: String?): Boolean {
                    findItems(newText)
                    viewDataBinding?.recyclerViewItems?.scrollToPosition(0)
                    return false
                }
            })
        if (viewModel.cachedSearchQuery.isNullOrEmpty()) {
            (viewDataBinding?.toolbar?.menu?.findItem(R.id.menu_search)?.actionView as SearchView)
                .isIconified = true
        }
        super.onResume()
    }

    private fun setupOptionsMenu() {
        viewModel.currentGroup.observe(
            viewLifecycleOwner,
            Observer {
                val menuItem = viewDataBinding?.toolbar?.menu?.findItem(R.id.menu_delete_group)
                menuItem?.isEnabled = it != getString(R.string.all_items)
            }
        )
        viewDataBinding?.toolbar?.setOnMenuItemClickListener(
            object : Toolbar.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    return when (item?.itemId) {
                        R.id.menu_search -> true
                        R.id.menu_sort -> {
                            val popup = PopupMenu(
                                requireContext(),
                                requireActivity()
                                    .findViewById(item.itemId)
                            )
                            popup.menuInflater.inflate(R.menu.popup_sort_menu, popup.menu)
                            popup.show()
                            popup.setOnMenuItemClickListener {
                                return@setOnMenuItemClickListener when (it.itemId) {
                                    R.id.by_name_asc -> {
                                        sortList(SortingMode.BY_NAME_ASC)
                                        true
                                    }
                                    R.id.by_name_desc -> {
                                        sortList(SortingMode.BY_NAME_DESC)
                                        true
                                    }
                                    R.id.by_date_asc -> {
                                        sortList(SortingMode.BY_DATE_ASC)
                                        true
                                    }
                                    R.id.by_date_desc -> {
                                        sortList(SortingMode.BY_DATE_DESC)
                                        true
                                    }
                                    R.id.by_orders_count_desc -> {
                                        sortList(SortingMode.BY_ORDERS_COUNT)
                                        true
                                    }
                                    R.id.by_orders_count_per_day_desc -> {
                                        sortList(SortingMode.BY_ORDERS_COUNT_PER_DAY)
                                        true
                                    }
                                    else -> false
                                }
                            }
                            true
                        }
                        R.id.menu_refresh -> {
                            viewModel.updateItems()
                            true
                        }
                        R.id.menu_new_group -> {
                            viewModel.newGroup()
                            true
                        }
                        R.id.menu_delete_group -> {
                            viewModel.deleteGroup()
                            true
                        }
                        R.id.menu_backup -> {
                            viewModel.signIn()
                            true
                        }
                        R.id.menu_theme -> {
                            viewModel.changeTheme()
                            true
                        }
                        else -> false
                    }
                }
            })
    }

    private fun setupSpinner() {
        viewDataBinding?.toolbar?.title = null

        val groupNames = viewModel.getSavedGroupNames()

        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            groupNames
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            viewDataBinding?.spinner?.adapter = it
        }

        viewDataBinding?.spinner?.setSelection(groupNames.indexOf(viewModel.currentGroup.value))

        viewDataBinding?.spinner?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) { }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (view != null) {
                        viewModel.changeCurrentGroup(groupNames[position])
                    }
                }
            }
    }

    private fun setupRecycler() {

        viewDataBinding?.recyclerViewItems?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = ItemsAdapter(viewModel, viewModel.getItemsComparator())
            adapter!!.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }

        mItemKeyProvider = MyItemKeyProvider()

        tracker = SelectionTracker.Builder(
            "id",
            viewDataBinding!!.recyclerViewItems,
            mItemKeyProvider!!,
            MyItemDetailsLookup(viewDataBinding!!.recyclerViewItems),
            StorageStrategy.createStringStorage()
        ).build()

        (viewDataBinding?.recyclerViewItems?.adapter as ItemsAdapter).tracker = tracker

        tracker?.addObserver(object : SelectionTracker.SelectionObserver<String>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()
                if (tracker!!.hasSelection() && actionMode == null) {
                    startActionMode()
                } else if (!tracker!!.hasSelection()) {
                    actionMode?.finish()
                } else {
                    setSelectedTitle(tracker!!.selection.size())
                }
            }
        })

        itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.RIGHT
        ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ) = false
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    (viewDataBinding?.recyclerViewItems?.adapter as ItemsAdapter).sortedList
                        .get(viewHolder.bindingAdapterPosition)?.let { viewModel.editItem(it.id) }
                }
                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    val card = viewHolder.itemView.findViewById<CardView>(R.id.card)
                    c.clipRect(0f, card.top.toFloat(), dX, card.bottom.toFloat())
                    val editIcon = getDrawable(
                        resources,
                        R.drawable.ic_baseline_edit_24,
                        requireActivity().theme
                    )
                    if (editIcon != null) {
                        editIcon.setTint(requireContext().themeColor(R.attr.colorOnBackground))
                        val rect = Rect(
                            (card.height - editIcon.intrinsicHeight) / 4,
                            card.top + (card.height - editIcon.intrinsicHeight) / 2,
                            editIcon.intrinsicWidth + (card.height - editIcon.intrinsicHeight) / 4,
                            card.top + editIcon.intrinsicHeight + (
                                card.height -
                                    editIcon.intrinsicHeight
                                ) / 2
                        )
                        editIcon.bounds = rect
                        editIcon.draw(c)
                    }
                    super.onChildDraw(
                        c, recyclerView, viewHolder, dX, dY, actionState,
                        isCurrentlyActive
                    )
                }
            })

        setItemTouchHelperEnabled(true)
    }

    private fun setItemTouchHelperEnabled(isEnabled: Boolean) {
        if (isEnabled) {
            itemTouchHelper?.attachToRecyclerView(viewDataBinding?.recyclerViewItems)
        } else {
            itemTouchHelper?.attachToRecyclerView(null)
        }
    }

    private fun setupNavigation() {
        viewModel.openItemEvent.observe(
            viewLifecycleOwner,
            EventObserver {
                exitTransition = MaterialElevationScale(false)
                reenterTransition = MaterialElevationScale(true)
                val viewHolder = viewDataBinding!!.recyclerViewItems
                    .findViewHolderForAdapterPosition(it) as ItemsAdapter.ItemsViewHolder
                val itemId = (viewDataBinding!!.recyclerViewItems.adapter as ItemsAdapter)
                    .sortedList[it].id
                val extras = FragmentNavigatorExtras(
                    viewHolder.recyclerViewItemBinding.card to
                        getString(R.string.shared_element_container_detail_fragment)
                )
                val action = ItemsFragmentDirections.actionItemsFragmentToDetailFragment(itemId)
                navigate(action, extras)
            }
        )

        viewModel.addItemEvent.observe(
            viewLifecycleOwner,
            EventObserver {
                exitTransition = MaterialElevationScale(false)
                reenterTransition = MaterialElevationScale(true)
                val action = ItemsFragmentDirections.actionItemsFragmentToAddItemFragment(
                    viewModel.currentGroup.value!!, it.value
                )
                navigate(
                    action,
                    FragmentNavigatorExtras(
                        viewDataBinding!!.fabAdditem to
                            getString(R.string.shared_element_container_add_item)
                    )
                )
            }
        )

        viewModel.confirmDeleteEvent.observe(
            viewLifecycleOwner,
            EventObserver {
                val action = ItemsFragmentDirections
                    .actionItemsFragmentToConfirmRemoveDialogFragment2(it.toTypedArray())
                navigate(action)
            }
        )

        viewModel.editItemEvent.observe(
            viewLifecycleOwner,
            EventObserver {
                exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
                reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
                val action = ItemsFragmentDirections.actionItemsFragmentToEditItemFragment(it)
                actionMode?.finish()
                navigate(action)
            }
        )

        viewModel.updateErrorEvent.observe(
            viewLifecycleOwner,
            EventObserver {
                val action = ItemsFragmentDirections.actionItemsFragmentToErrorDialogFragment(it)
                navigate(action)
            }
        )

        viewModel.newGroupEvent.observe(
            viewLifecycleOwner,
            EventObserver {
                val action = ItemsFragmentDirections.actionItemsFragmentToNewGroupDialogFragment()
                navigate(action)
            }
        )

        viewModel.deleteGroupEvent.observe(
            viewLifecycleOwner,
            EventObserver {
                val action = ItemsFragmentDirections
                    .actionItemsFragmentToConfirmDeleteGroupDialogFrag(it)
                navigate(action)
            }
        )

        viewModel.addToGroupEvent.observe(
            viewLifecycleOwner,
            EventObserver {
                closeActionModeLater = true
                val action = ItemsFragmentDirections
                    .actionItemsFragmentToGroupPickerDialogFragment(it.toTypedArray())
                navigate(action)
            }
        )

        viewModel.signInEvent.observe(
            viewLifecycleOwner,
            EventObserver {
                exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
                reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
                val action = ItemsFragmentDirections.actionItemsFragmentToSignInFragment()
                navigate(action)
            }
        )

        viewModel.changeThemeEvent.observe(
            viewLifecycleOwner,
            EventObserver {
                exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
                reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
                val action = ItemsFragmentDirections.actionItemsFragmentToThemeSelectorFragment()
                navigate(action)
            }
        )
    }

    private fun setSelectedTitle(selected: Int) {
        actionMode?.title = getString(R.string.selected) + selected
    }

    private fun startActionMode() {
        actionMode = viewDataBinding?.toolbar?.startActionMode(object : ActionMode.Callback {
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                val itemsId = tracker!!.selection.toList()
                return when (item?.itemId) {
                    R.id.menu_delete -> {
                        viewModel.confirmDelete(itemsId)
                        true
                    }
                    R.id.menu_add_to_group -> {
                        viewModel.addToGroup(itemsId)
                        true
                    }
                    else -> false
                }
            }

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                mode?.menuInflater?.inflate(R.menu.selection_menu, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = true

            override fun onDestroyActionMode(mode: ActionMode?) {
                tracker?.clearSelection()
                viewDataBinding?.fabAdditem?.show()
                actionMode = null
                viewDataBinding?.swipeRefresh?.isEnabled = true
                setItemTouchHelperEnabled(true)
            }
        })
        viewDataBinding?.fabAdditem?.hide()
        setSelectedTitle(tracker!!.selection.size())
        viewDataBinding?.swipeRefresh?.isEnabled = false
        setItemTouchHelperEnabled(false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        tracker?.onSaveInstanceState(outState)
    }

    private fun setupSearchView() {
        val searchView = viewDataBinding?.toolbar?.menu?.findItem(R.id.menu_search)
            ?.actionView as SearchView
        searchView.apply {
            // make editText not expanded in landscape mode
            imeOptions = searchView.imeOptions or EditorInfo.IME_FLAG_NO_EXTRACT_UI
            val menu = viewDataBinding?.toolbar?.menu
            val menuItems = setOf(
                R.id.menu_sort, R.id.menu_refresh, R.id.menu_new_group,
                R.id.menu_delete_group, R.id.menu_backup, R.id.menu_theme
            )
            setOnSearchClickListener {
                menuItems.forEach { menu?.findItem(it)?.isVisible = false }
                viewDataBinding?.spinner?.isVisible = false
                viewDataBinding?.swipeRefresh?.isEnabled = false
            }
            setOnCloseListener {
                menuItems.forEach { menu?.findItem(it)?.isVisible = true }
                viewDataBinding?.spinner?.isVisible = true
                viewDataBinding?.swipeRefresh?.isEnabled = true
                false
            }
        }
    }

    private fun sortList(sortingMode: SortingMode) {
        val adapter = viewDataBinding?.recyclerViewItems?.adapter as ItemsAdapter
        viewModel.saveSortingMode(sortingMode)
        adapter.comparator = viewModel.getItemsComparator()
        val items = viewModel.items.value
        if (items != null) {
            adapter.replaceAll(items)
        }

        viewDataBinding?.recyclerViewItems?.scrollToPosition(0)
    }

    private fun findItems(query: String?) {
        val filteredList = viewModel.filterItems(query)
        (viewDataBinding?.recyclerViewItems?.adapter as ItemsAdapter).replaceAll(filteredList)
    }
}
