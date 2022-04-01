package com.automotivecodelab.wbgoodstracker.ui.itemsfrag

import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat.getDrawable
import androidx.core.view.*
import androidx.core.widget.PopupMenuCompat
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

    private var closeActionModeLater = false
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
        viewModel.itemsWithCurrentGroup.observe(viewLifecycleOwner) { (items, _) ->
            adapter?.replaceAll(items)
            if (scrollToStartOnUpdate) {
                viewDataBinding?.recyclerViewItems?.scrollToPosition(0)
                scrollToStartOnUpdate = false
            }

            // called when user choose group in group picker
            if (closeActionModeLater) {
                closeActionModeLater = false
                viewModel.clearSelection()
            }

            view.doOnPreDraw { startPostponedEnterTransition() }
        }
    }

    private fun setupOptionsMenu() {
        viewModel.itemsWithCurrentGroup.observe(viewLifecycleOwner) { (_, group) ->
            viewDataBinding?.toolbar?.menu?.findItem(R.id.menu_delete_group)?.isEnabled =
                group != null
        }
        viewDataBinding?.toolbar?.setOnMenuItemClickListener(
            object : Toolbar.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    return when (item?.itemId) {
                        R.id.menu_search -> false
                        R.id.menu_sort -> {
                            val popup = PopupMenu(
                                requireContext(),
                                requireView().findViewById(item.itemId)
                            )
                            popup.menuInflater.inflate(R.menu.popup_sort_menu, popup.menu)
                            val sortingModeToMenuItemMap = mapOf(
                                R.id.by_name_asc to SortingMode.BY_NAME_ASC,
                                R.id.by_name_desc to SortingMode.BY_NAME_DESC,
                                R.id.by_date_asc to SortingMode.BY_DATE_ASC,
                                R.id.by_date_desc to SortingMode.BY_DATE_DESC,
                                R.id.by_orders_count_desc to SortingMode.BY_ORDERS_COUNT,
                                R.id.by_orders_count_per_day_desc to
                                        SortingMode.BY_ORDERS_COUNT_PER_DAY
                            )
                            popup.setOnMenuItemClickListener { menuItem ->
                                sortingModeToMenuItemMap[menuItem.itemId]?.let { sortingMode ->
                                    viewModel.setSortingMode(sortingMode)
                                }
                                scrollToStartOnUpdate = true
                                return@setOnMenuItemClickListener true
                            }
                            popup.show()
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

    private fun setupActionMode() {
        var actionMode: ActionMode? = null
        val callback = object : ActionMode.Callback {
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return when (item?.itemId) {
                    R.id.menu_delete -> {
                        viewModel.confirmDelete()
                        true
                    }
                    R.id.menu_add_to_group -> {
                        viewModel.addToGroup()
                        true
                    }
                    R.id.menu_edit -> {
                        viewModel.editItem()
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
                viewModel.clearSelection()
            }
        }

        adapter?.tracker?.run {
            addObserver(object : SelectionTracker.SelectionObserver<String>() {
                override fun onItemStateChanged(key: String, selected: Boolean) {
                    super.onItemStateChanged(key, selected)
                    if (selected) viewModel.selectItem(key)
                    else viewModel.unselectItem(key)
                }
            })
        }

        viewModel.selectedItemIds.observe(viewLifecycleOwner) { selectedItemsSet ->
            if (selectedItemsSet.isEmpty()) {
                actionMode?.finish()
                actionMode = null
                setItemTouchHelperEnabled(true)
                if (viewDataBinding?.toolbar?.menu?.findItem(R.id.menu_search)
                        ?.isActionViewExpanded == false) {
                            viewDataBinding?.fabAdditem?.show()
                            viewDataBinding?.swipeRefresh?.isEnabled = true
                }
                adapter?.tracker?.clearSelection()
            } else {
                if (actionMode == null) {
                    actionMode = viewDataBinding?.toolbar?.startActionMode(callback)
                    viewDataBinding?.fabAdditem?.hide()
                    viewDataBinding?.swipeRefresh?.isEnabled = false
                    setItemTouchHelperEnabled(false)
                    adapter?.tracker?.setItemsSelected(selectedItemsSet, true)
                }
                actionMode?.title = getString(R.string.selected) + selectedItemsSet.size
                actionMode?.menu?.findItem(R.id.menu_edit)?.isEnabled =
                    selectedItemsSet.size == 1
            }
        }
    }

    private fun setupSpinner() {
        viewDataBinding?.toolbar?.title = null
        val defaultGroup = requireContext().getString(R.string.all_items)
        val groups = mutableListOf(defaultGroup)

        viewDataBinding?.spinner?.setOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            groups.forEach { group ->
                popup.menu.add(group)
            }
            popup.setOnMenuItemClickListener { menuItem ->
                val selectedText = menuItem.title
                if (selectedText != null) {
                    val group = if (selectedText == defaultGroup) null
                        else selectedText.toString()
                    if (group != viewModel.itemsWithCurrentGroup.value?.second) {
                        viewModel.setCurrentGroup(group)
                        scrollToStartOnUpdate = true
                    }
                }
                true
            }
            popup.show()
        }

        viewModel.groups.observe(viewLifecycleOwner) { savedGroups ->
            val groupsToAdd = savedGroups.minus(groups)
            val groupsToRemove = groups.minus(savedGroups).minus(defaultGroup)
            groups.addAll(groupsToAdd)
            groups.removeAll(groupsToRemove)
        }
        viewModel.itemsWithCurrentGroup.observe(viewLifecycleOwner) { (_, group) ->
            viewDataBinding?.spinner?.text = group ?: defaultGroup
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

        setupItemTouchHelper()
        setupActionMode()
        setItemTouchHelperEnabled(true)

        viewModel.itemsComparator.observe(viewLifecycleOwner) { comparator ->
            adapter?.setItemsComparator(comparator)
            if (scrollToStartOnUpdate) {
                viewDataBinding?.recyclerViewItems?.scrollToPosition(0)
                scrollToStartOnUpdate = false
            }
        }
    }

    private fun setupItemTouchHelper() {
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
                                    viewModel.deleteSingleItem(item.id)
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
    }

    private fun setItemTouchHelperEnabled(isEnabled: Boolean) {
        if (isEnabled) {
            itemTouchHelper?.attachToRecyclerView(viewDataBinding?.recyclerViewItems)
        } else {
            itemTouchHelper?.attachToRecyclerView(null)
        }
    }

    private fun setupNavigation() {
        viewModel.openItemEvent.observe(viewLifecycleOwner, EventObserver { recyclerItemPosition ->
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

        viewModel.addItemEvent.observe(viewLifecycleOwner, EventObserver {
                exitTransition = MaterialElevationScale(false)
                reenterTransition = MaterialElevationScale(true)
                val action = MainNavDirections.actionGlobalAddItemFragment(null)
                navigate(
                    action,
                    FragmentNavigatorExtras(
                        viewDataBinding!!.fabAdditem to
                            getString(R.string.shared_element_container_add_item)
                    )
                )
            }
        )

        viewModel.confirmDeleteEvent.observe(viewLifecycleOwner, EventObserver {
                val action = ItemsFragmentDirections
                    .actionItemsFragmentToConfirmRemoveDialogFragment2(it.toTypedArray())
                navigate(action)
            }
        )

        viewModel.editItemEvent.observe(viewLifecycleOwner, EventObserver {
                exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
                reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
                val action = ItemsFragmentDirections.actionItemsFragmentToEditItemFragment(it)
                viewModel.clearSelection()
                navigate(action)
            }
        )

        viewModel.updateErrorEvent.observe(viewLifecycleOwner, EventObserver {
                val action = ItemsFragmentDirections.actionItemsFragmentToErrorDialogFragment(it)
                navigate(action)
            }
        )

        viewModel.deleteGroupEvent.observe(viewLifecycleOwner, EventObserver {
                val action = ItemsFragmentDirections
                    .actionItemsFragmentToConfirmDeleteGroupDialogFrag(it)
                navigate(action)
            }
        )

        viewModel.addToGroupEvent.observe(viewLifecycleOwner, EventObserver {
                closeActionModeLater = true
                val action = ItemsFragmentDirections
                    .actionItemsFragmentToGroupPickerDialogFragment(it.toTypedArray())
                navigate(action)
            }
        )

        viewModel.signInEvent.observe(viewLifecycleOwner, EventObserver {
                exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
                reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
                val action = ItemsFragmentDirections.actionItemsFragmentToSignInFragment()
                navigate(action)
            }
        )

        viewModel.changeThemeEvent.observe(viewLifecycleOwner, EventObserver {
                exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
                reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
                val action = ItemsFragmentDirections.actionItemsFragmentToThemeSelectorFragment()
                navigate(action)
            }
        )
    }

    private fun setupSearchView() {
        viewDataBinding?.toolbar?.menu?.findItem(R.id.menu_search)?.apply {
            (actionView as? SearchView)?.let { searchView ->
                // make editText not expanded in landscape mode
                searchView.imeOptions = searchView.imeOptions or EditorInfo.IME_FLAG_NO_EXTRACT_UI

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?) = false
                    override fun onQueryTextChange(newText: String?): Boolean {
                        viewModel.filterItems(newText ?: "")
                        if (adapter?.itemCount != 0) scrollToStartOnUpdate = true
                        return true
                    }
                })
                viewDataBinding?.toolbar?.doOnLayout { toolbar ->
                    searchView.maxWidth = toolbar.width
                }
            }
            setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                    viewDataBinding?.toolbar?.menu?.forEach { menuItem ->
                        if (menuItem.itemId != R.id.menu_search) {
                            menuItem.isVisible = false
                        }
                    }
                    viewDataBinding?.spinner?.isVisible = false
                    viewDataBinding?.swipeRefresh?.isEnabled = false
                    viewDataBinding?.fabAdditem?.hide()
                    return true
                }

                override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                    viewDataBinding?.toolbar?.menu?.forEach { menuItem ->
                        if (menuItem.itemId != R.id.menu_search) {
                            menuItem.isVisible = true
                        }
                    }
                    viewDataBinding?.spinner?.isVisible = true
                    viewDataBinding?.swipeRefresh?.isEnabled = true
                    viewDataBinding?.fabAdditem?.show()
                    return true
                }
            })
        }
    }
}
