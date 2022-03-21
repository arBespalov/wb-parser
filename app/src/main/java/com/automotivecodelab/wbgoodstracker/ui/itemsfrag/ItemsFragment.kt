package com.automotivecodelab.wbgoodstracker.ui.itemsfrag

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat.getDrawable
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.automotivecodelab.wbgoodstracker.*
import com.automotivecodelab.wbgoodstracker.databinding.ItemsFragmentBinding
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import com.automotivecodelab.wbgoodstracker.ui.EventObserver
import com.automotivecodelab.wbgoodstracker.ui.SignOutSnackbar
import com.automotivecodelab.wbgoodstracker.ui.itemsfrag.recyclerview.ItemsAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialSharedAxis

class ItemsFragment : Fragment() {

    private val viewModel: ItemsViewModel by viewModels {
        ItemsViewModelFactory(getItemsRepository(), getUserRepository(), getSortRepository())
    }

    // references to views
    private var viewDataBinding: ItemsFragmentBinding? = null
    private var adapter: ItemsAdapter? = null
    private var itemTouchHelper: ItemTouchHelper? = null
    private var actionMode: ActionMode? = null

    private var actionModeRestored = false
    private var closeActionModeLater = false
    // to scroll on group change
    private var scrollToStartOnUpdate = false

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
        postponeEnterTransition()
        return view
    }

    override fun onDestroyView() {
        viewDataBinding = null
        adapter = null
        itemTouchHelper = null
        actionMode = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        viewDataBinding?.toolbar?.setupWithNavController(navController, appBarConfiguration)

        setupRecycler()
        setupSpinner()
        setupSearchView()
        setupOptionsMenu()
        setupNavigation()

        viewDataBinding?.fabAdditem?.setOnClickListener {
            viewModel.addItem()
        }
        viewDataBinding?.swipeRefresh?.setOnRefreshListener {
            viewModel.updateItems()
            scrollToStartOnUpdate = true
        }
        viewModel.dataLoading.observe(viewLifecycleOwner) {
            viewDataBinding?.swipeRefresh?.isRefreshing = it
        }
        viewModel.authorizationErrorEvent.observe(viewLifecycleOwner, EventObserver {
            SignOutSnackbar().invoke(requireView()) { viewModel.signOut() }
        })
        viewModel.itemsWithCurrentGroup.observe(viewLifecycleOwner) { itemsWithCurrentGroup ->
            if (itemsWithCurrentGroup?.first != null) {
                adapter?.replaceAll(itemsWithCurrentGroup.first)
                if (scrollToStartOnUpdate) {
                    viewDataBinding?.recyclerViewItems?.scrollToPosition(0)
                    scrollToStartOnUpdate = false
                }
            }

            // items must be set before performing search
            if (!viewModel.cachedSearchQuery.isNullOrEmpty()) {
                (viewDataBinding?.toolbar?.menu?.findItem(R.id.menu_search)?.actionView as
                        SearchView).apply {
                    isIconified = false
                    setQuery(viewModel.cachedSearchQuery, false)
                }
                findItems(viewModel.cachedSearchQuery)
            }

            // called when user choose group in group picker
            if (closeActionModeLater) {
                closeActionModeLater = false
                actionMode?.finish()
            }

            // handle rotation
            if (savedInstanceState != null && !actionModeRestored) {
                actionModeRestored = true
                adapter?.tracker?.onRestoreInstanceState(savedInstanceState)
                if (adapter?.tracker?.hasSelection() == true && actionMode == null) {
                    startActionMode()
                }
            }
        }
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    // if set listener in onViewCreated, onQueryTextChange triggers immediately and causes
    // list to scroll up even after pressing back on details screen
    override fun onResume() {
        (viewDataBinding?.toolbar?.menu?.findItem(R.id.menu_search)?.actionView as SearchView)
            .setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?) = false
                override fun onQueryTextChange(newText: String?): Boolean {
                    findItems(newText)
                    viewDataBinding?.recyclerViewItems?.scrollToPosition(0)
                    return false
                }
            })
        super.onResume()
    }

    private fun setupOptionsMenu() {
        viewModel.itemsWithCurrentGroup.observe(viewLifecycleOwner) { itemsWithGroup ->
            viewDataBinding?.toolbar?.menu?.findItem(R.id.menu_delete_group)?.run {
                isEnabled = itemsWithGroup.second != null
            }
        }
        viewDataBinding?.toolbar?.setOnMenuItemClickListener(
            object : Toolbar.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    return when (item?.itemId) {
                        R.id.menu_search -> true
                        R.id.menu_sort -> {
                            val popup = PopupMenu(
                                requireContext(),
                                requireActivity().findViewById(item.itemId)
                            )
                            popup.menuInflater.inflate(R.menu.popup_sort_menu, popup.menu)
                            popup.show()
                            popup.setOnMenuItemClickListener {
                                when (it.itemId) {
                                    R.id.by_name_asc -> {
                                        viewModel.setSortingMode(SortingMode.BY_NAME_ASC)
                                    }
                                    R.id.by_name_desc -> {
                                        viewModel.setSortingMode(SortingMode.BY_NAME_DESC)
                                    }
                                    R.id.by_date_asc -> {
                                        viewModel.setSortingMode(SortingMode.BY_DATE_ASC)
                                    }
                                    R.id.by_date_desc -> {
                                        viewModel.setSortingMode(SortingMode.BY_DATE_DESC)
                                    }
                                    R.id.by_orders_count_desc -> {
                                        viewModel.setSortingMode(SortingMode.BY_ORDERS_COUNT)
                                    }
                                    R.id.by_orders_count_per_day_desc -> {
                                        viewModel.setSortingMode(
                                            SortingMode.BY_ORDERS_COUNT_PER_DAY)
                                    }
                                }
                                scrollToStartOnUpdate = true
                                return@setOnMenuItemClickListener true
                            }
                            true
                        }
                        R.id.menu_refresh -> {
                            viewModel.updateItems()
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
            }
        )
    }

    private fun setupSpinner() {
        viewDataBinding?.toolbar?.title = null
        val defaultGroup = requireContext().getString(R.string.all_items)
        val groups = mutableListOf(defaultGroup)
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            groups
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        viewDataBinding?.spinner?.adapter = spinnerAdapter
        viewDataBinding?.spinner?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) { }
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val textView = view as? TextView
                    if (textView != null) {
                        if (textView.text == requireContext().getString(R.string.all_items)) {
                            viewModel.setCurrentGroup(null)
                        } else {
                            viewModel.setCurrentGroup(textView.text.toString())
                        }
                        scrollToStartOnUpdate = true
                    }
                }
            }

        viewModel.groups.observe(viewLifecycleOwner) { savedGroups ->
            val groupsToAdd = savedGroups.minus(groups)
            val groupsToRemove = groups.minus(savedGroups.plus(defaultGroup))
            groups.addAll(groupsToAdd)
            groups.removeAll(groupsToRemove)

            viewModel.itemsWithCurrentGroup.observe(viewLifecycleOwner) { itemsWithGroup ->
                viewDataBinding?.spinner?.setSelection(
                    groups.indexOf(itemsWithGroup.second),
                    true
                )
            }
        }
    }

    private fun setupRecycler() {
        adapter = ItemsAdapter(
            comparator = null,
            onOpenItemDetails = viewModel::openItem
        )
        adapter?.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy
            .PREVENT_WHEN_EMPTY
        viewDataBinding?.recyclerViewItems?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = this@ItemsFragment.adapter
        }
        adapter?.tracker?.run {
            addObserver(object : SelectionTracker.SelectionObserver<String>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()
                    if (hasSelection() && actionMode == null) {
                        startActionMode()
                    } else if (!hasSelection()) {
                        actionMode?.finish()
                    } else {
                        setSelectedTitle(selection.size())
                    }
                }
            })
        }
        itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                (viewHolder as? ItemsAdapter.ItemViewHolder)?.recyclerViewItemBinding?.item
                ?.let { item ->
                    val isItemInFirstPosition = viewHolder.bindingAdapterPosition == 0
                    adapter?.remove(item)
                    val snackbar = Snackbar.make(
                        viewDataBinding?.fabAdditem ?: requireView(),
                        R.string.item_deleted,
                        Snackbar.LENGTH_LONG)
                    snackbar.setAction(R.string.undo) {
                        adapter?.add(item)
                        if (isItemInFirstPosition)
                            viewDataBinding?.recyclerViewItems?.scrollToPosition(0)
                    }
                    snackbar.addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            if (event != DISMISS_EVENT_ACTION) {
                                viewModel.deleteItem(item.id)
                            }
                            viewDataBinding?.fabAdditem?.show()
                            super.onDismissed(transientBottomBar, event)
                        }
                    })
                    viewDataBinding?.fabAdditem?.hide()
                    snackbar.show()
                }
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
                val card = (viewHolder as ItemsAdapter.ItemViewHolder).recyclerViewItemBinding.card
                c.clipRect(
                    card.right + dX,
                    card.top.toFloat(),
                    card.right.toFloat(),
                    card.bottom.toFloat()
                )
                val editIcon = getDrawable(
                    resources,
                    R.drawable.ic_baseline_delete_24,
                    requireActivity().theme
                )
                if (editIcon != null) {
                    editIcon.setTint(requireContext().themeColor(R.attr.colorOnBackground))
                    val rect = Rect(
                        card.right - editIcon.intrinsicWidth - (card.height - editIcon
                            .intrinsicHeight) / 4,
                        card.top + (card.height - editIcon.intrinsicHeight) / 2,
                        card.right - (card.height - editIcon.intrinsicHeight) / 4,
                        card.top + editIcon.intrinsicHeight + (card.height -
                                editIcon.intrinsicHeight) / 2
                    )
                    editIcon.bounds = rect
                    //c.drawColor(Color.BLUE)
                    editIcon.draw(c)
                }
                super.onChildDraw(
                    c, recyclerView, viewHolder, dX, dY, actionState,
                    isCurrentlyActive
                )
            }
        })

        setItemTouchHelperEnabled(true)

        viewModel.itemsComparator.observe(viewLifecycleOwner) { comparator ->
            adapter?.setItemsComparator(comparator)
            if (scrollToStartOnUpdate) {
                viewDataBinding?.recyclerViewItems?.scrollToPosition(0)
                scrollToStartOnUpdate = false
            }
        }
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
            EventObserver { recyclerItemPosition ->
                exitTransition = MaterialElevationScale(false)
                reenterTransition = MaterialElevationScale(true)
                val viewHolder = viewDataBinding!!.recyclerViewItems
                    .findViewHolderForAdapterPosition(recyclerItemPosition)
                        as ItemsAdapter.ItemViewHolder
                val itemId = viewHolder.recyclerViewItemBinding.item!!.id
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
                val action = ItemsFragmentDirections.actionItemsFragmentToAddItemFragment(null)
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
        actionMode?.menu?.findItem(R.id.menu_edit)?.isEnabled = selected == 1
    }

    private fun startActionMode() {
        actionMode = viewDataBinding?.toolbar?.startActionMode(object : ActionMode.Callback {
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                val itemsId = adapter?.tracker?.selection?.toList() ?: emptyList()
                return when (item?.itemId) {
                    R.id.menu_delete -> {
                        viewModel.confirmDelete(itemsId)
                        true
                    }
                    R.id.menu_add_to_group -> {
                        viewModel.addToGroup(itemsId)
                        true
                    }
                    R.id.menu_edit -> {
                        viewModel.editItem(itemsId[0])
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
                adapter?.tracker?.clearSelection()
                viewDataBinding?.fabAdditem?.show()
                actionMode = null
                viewDataBinding?.swipeRefresh?.isEnabled = true
                setItemTouchHelperEnabled(true)
            }
        })
        viewDataBinding?.fabAdditem?.hide()
        setSelectedTitle(adapter?.tracker?.selection?.size() ?: 0)
        viewDataBinding?.swipeRefresh?.isEnabled = false
        setItemTouchHelperEnabled(false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        adapter?.tracker?.onSaveInstanceState(outState)
    }

    private fun setupSearchView() {
        (viewDataBinding?.toolbar?.menu?.findItem(R.id.menu_search)?.actionView as SearchView)
            .apply {
                // make editText not expanded in landscape mode
                imeOptions = imeOptions or EditorInfo.IME_FLAG_NO_EXTRACT_UI
                val menu = viewDataBinding?.toolbar?.menu
                val menuItems = setOf(
                    R.id.menu_sort,
                    R.id.menu_refresh,
                    R.id.menu_delete_group,
                    R.id.menu_backup,
                    R.id.menu_theme
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

    private fun findItems(query: String?) {
        val filteredList = viewModel.filterItems(query)
        adapter?.replaceAll(filteredList)
    }
}
