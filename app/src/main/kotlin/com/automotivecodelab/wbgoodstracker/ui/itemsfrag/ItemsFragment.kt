package com.automotivecodelab.wbgoodstracker.ui.itemsfrag

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat.getDrawable
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.automotivecodelab.wbgoodstracker.*
import com.automotivecodelab.wbgoodstracker.databinding.ItemsFragmentBinding
import com.automotivecodelab.wbgoodstracker.domain.models.Ad
import com.automotivecodelab.wbgoodstracker.domain.models.SortingMode
import com.automotivecodelab.wbgoodstracker.ui.EventObserver
import com.automotivecodelab.wbgoodstracker.ui.ViewModelFactory
import com.automotivecodelab.wbgoodstracker.ui.itemsfrag.recyclerview.HeaderAdapter
import com.automotivecodelab.wbgoodstracker.ui.itemsfrag.recyclerview.ItemsAdapter
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialSharedAxis
import com.google.android.play.core.review.ReviewManagerFactory
import timber.log.Timber

class ItemsFragment : Fragment() {

    private val viewModel: ItemsViewModel by viewModels {
        ViewModelFactory(requireContext().appComponent.itemsViewModel())
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
        viewModel.authorizationErrorEvent.observe(
            viewLifecycleOwner,
            EventObserver {
                requireView().syncErrorSnackbar()
            }
        )
        viewModel.askUserForReviewEvent.observe(
            viewLifecycleOwner,
            EventObserver {
                runCatching {
                    val manager = ReviewManagerFactory.create(requireContext())
                    manager.requestReviewFlow().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val reviewInfo = task.result
                            manager.launchReviewFlow(requireActivity(), reviewInfo)
                        } else {
                            Timber.d(task.exception)
                        }
                    }
                }
            }
        )

        viewModel.itemGroups.observe(viewLifecycleOwner) { itemGroups ->
            if (itemGroups.totalItemsQuantity == 0) {
                viewDataBinding?.apply {
                    emptyListHint.setText(R.string.empty_list_hint)
                    emptyListHint.visibility = View.VISIBLE
                    toolbar.menu?.findItem(R.id.menu_refresh)?.isEnabled = false
                    toolbar.menu?.findItem(R.id.menu_search)?.isEnabled = false
                    // disable toolbar moving on scroll
                    swipeRefresh.visibility = View.INVISIBLE
                }
            } else {
                viewDataBinding?.apply {
                    emptyListHint.visibility = View.GONE
                    toolbar.menu?.findItem(R.id.menu_refresh)?.isEnabled = true
                    toolbar.menu?.findItem(R.id.menu_search)?.isEnabled = true
                    swipeRefresh.visibility = View.VISIBLE
                }
            }
        }

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
            viewDataBinding?.toolbar?.menu?.apply {
                val isEnabled = group != null
                findItem(R.id.menu_delete_group)?.isEnabled = isEnabled
                findItem(R.id.menu_rename_group)?.isEnabled = isEnabled
            }
        }
        viewDataBinding?.toolbar?.setOnMenuItemClickListener { item ->
            when (item?.itemId) {
                R.id.menu_search -> false
                R.id.menu_sort -> {
                    showSortingPopupMenu(requireView().findViewById(R.id.menu_sort))
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
                R.id.menu_rename_group -> {
                    viewModel.renameGroup()
                    true
                }
                R.id.menu_contacts -> {
                    viewModel.showContacts()
                    true
                }
                else -> false
            }
        }
    }

    private fun showSortingPopupMenu(anchor: View) {
        val popup = PopupMenu(
            requireContext(),
            anchor
        )
        popup.setForceShowIcon(true)
        SortingMode.values().forEachIndexed { index, sortingMode ->
            val stringId = when (sortingMode) {
                SortingMode.BY_NAME_ASC -> R.string.by_name_asc
                SortingMode.BY_NAME_DESC -> R.string.by_name_desc
                SortingMode.BY_DATE_ASC -> R.string.by_date_asc
                SortingMode.BY_DATE_DESC -> R.string.by_date_desc
                SortingMode.BY_ORDERS_COUNT -> R.string.by_orders_count
                SortingMode.BY_ORDERS_COUNT_PER_DAY ->
                    R.string.by_orders_count_per_day
                SortingMode.BY_LAST_CHANGES -> R.string.by_last_changes
            }
            popup.menu.add(
                0,
                sortingMode.ordinal,
                index,
                getString(stringId)
            )
        }

        popup.setOnMenuItemClickListener { menuItem ->
            viewModel.setSortingMode(SortingMode.values()[menuItem.itemId])
            scrollToStartOnUpdate = true
            true
        }

        viewModel.currentSortingModeWithItemsComparator
            .observe(viewLifecycleOwner) { (currentSortingMode, _) ->
                popup.menu.forEach { menuItem ->
                    val iconId = if (SortingMode.values()[menuItem.itemId] ==
                        currentSortingMode
                    )
                        R.drawable.ic_baseline_radio_button_checked_24
                    else
                        R.drawable.ic_baseline_radio_button_unchecked_24
                    val icon = getDrawable(
                        requireContext().resources,
                        iconId,
                        null
                    )
                    icon?.setTint(
                        requireContext().themeColor(R.attr.colorOnSurface)
                    )
                    menuItem.icon = icon
                }
            }
        popup.show()
    }

    private fun setupActionMode() {
        var actionMode: ActionMode? = null
        val callback = object : ActionMode.Callback {
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return when (item?.itemId) {
                    R.id.menu_delete -> {
                        viewModel.confirmDeleteSelected()
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
                    ?.isActionViewExpanded == false
                ) {
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
                actionMode?.title = getString(R.string.selected) + ": " + selectedItemsSet.size
                actionMode?.menu?.findItem(R.id.menu_edit)?.isEnabled = selectedItemsSet.size == 1
            }
        }
    }

    private fun setupSpinner() {
        viewDataBinding?.toolbar?.title = null
        val defaultGroup = requireContext().getString(R.string.all_items)

        viewModel.itemsWithCurrentGroup.observe(viewLifecycleOwner) { (_, group) ->
            viewDataBinding?.spinner?.text = group ?: defaultGroup
        }
        viewModel.itemGroups.observe(viewLifecycleOwner) { itemGroups ->
            viewDataBinding?.spinner?.setOnClickListener { view ->
                val popup = PopupMenu(requireContext(), view)
                val groups = itemGroups.groups.plus(
                    defaultGroup to itemGroups.totalItemsQuantity
                )
                groups.forEachIndexed { index, (group, count) ->
                    val spannableString = buildSpannedString {
                        bold { append("$count ") }
                        append(group)
                    }
                    popup.menu.add(0, index, index, spannableString)
                }
                popup.setOnMenuItemClickListener { menuItem ->
                    val group = if (groups[menuItem.itemId].first == defaultGroup)
                        null
                    else
                        groups[menuItem.itemId]
                    if (group?.first != viewModel.itemsWithCurrentGroup.value?.second) {
                        viewModel.setCurrentGroup(group?.first)
                        scrollToStartOnUpdate = true
                    }
                    true
                }
                popup.show()
            }
        }
    }

    private fun setupRecycler() {
        adapter = ItemsAdapter(
            comparator = null,
            onOpenItemDetails = ::openItem
        )
        adapter?.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy
            .PREVENT_WHEN_EMPTY

        val headerAdapter = HeaderAdapter()
        viewDataBinding?.recyclerViewItems?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = ConcatAdapter(headerAdapter, this@ItemsFragment.adapter)
        }

        setupItemTouchHelper()
        setupActionMode()
        setItemTouchHelperEnabled(true)

        viewModel.ad.observe(viewLifecycleOwner) { ad: Ad? ->
            if (ad != null)
                headerAdapter.setAd(ad)
            else
                headerAdapter.removeAd()
        }
        viewModel.currentSortingModeWithItemsComparator.observe(
            viewLifecycleOwner
        ) { (_, comparator) ->
            adapter?.setItemsComparator(comparator)
            if (scrollToStartOnUpdate) {
                viewDataBinding?.recyclerViewItems?.scrollToPosition(0)
                scrollToStartOnUpdate = false
            }
        }
    }

    private fun setupItemTouchHelper() {
        itemTouchHelper = ItemTouchHelper(object : PartialSwipeCallback() {
            override fun onSwipe(viewHolder: RecyclerView.ViewHolder) {
                val itemId = (viewHolder as ItemsAdapter.ItemViewHolder)
                    .recyclerViewItemBinding.item!!.id
                viewModel.confirmDelete(itemId)
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

    private fun openItem(vh: ItemsAdapter.ItemViewHolder) {
        exitTransition = MaterialElevationScale(false)
        reenterTransition = MaterialElevationScale(true)
        val itemId = vh.recyclerViewItemBinding.item!!.id
        val extras = FragmentNavigatorExtras(
            vh.recyclerViewItemBinding.card to
                    getString(R.string.shared_element_container_detail_fragment)
        )
        val action = ItemsFragmentDirections.actionItemsFragmentToDetailFragment(itemId)
        navigate(action, extras)
    }

    private fun setupNavigation() {
        viewModel.addItemEvent.observe(
            viewLifecycleOwner,
            EventObserver {
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
                viewModel.clearSelection()
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

        viewModel.renameCurrentGroupEvent.observe(
            viewLifecycleOwner,
            EventObserver {
                val action = ItemsFragmentDirections
                    .actionItemsFragmentToNewGroupDialogFragment(null, true)
                navigate(action)
            }
        )

        viewModel.showContactsEvent.observe(
            viewLifecycleOwner,
            EventObserver {
                val action = ItemsFragmentDirections.actionItemsFragmentToContactsDialogFragment()
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
