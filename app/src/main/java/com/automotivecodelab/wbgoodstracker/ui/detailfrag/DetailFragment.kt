package com.automotivecodelab.wbgoodstracker.ui.detailfrag

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.core.view.doOnPreDraw
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.automotivecodelab.wbgoodstracker.*
import com.automotivecodelab.wbgoodstracker.databinding.CardSizeLayoutBinding
import com.automotivecodelab.wbgoodstracker.databinding.DetailFragmentBinding
import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.ui.EventObserver
import com.google.android.material.elevation.ElevationOverlayProvider
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialSharedAxis
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class DetailFragment : Fragment() {

    private val args: DetailFragmentArgs by navArgs()
    private val viewModel: DetailViewModel by viewModels {
        DetailViewModelFactory(getItemsRepository(), args.itemid)
    }
    private var viewDataBinding: DetailFragmentBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            scrimColor = Color.TRANSPARENT
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.detail_fragment, container, false)

        viewDataBinding = DetailFragmentBinding.bind(view).apply {
            lifecycleOwner = viewLifecycleOwner
            viewmodel = viewModel
        }

        return view
    }

    override fun onDestroyView() {
        viewDataBinding = null
        super.onDestroyView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        viewDataBinding?.collapsingToolbar?.setupWithNavController(
            viewDataBinding!!.toolbar,
            navController,
            appBarConfiguration
        )

        // https://github.com/material-components/material-components-android/issues/617
        val isDarkTheme = requireContext().resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        if (isDarkTheme) {
            viewDataBinding?.collapsingToolbar?.setContentScrimColor(
                ElevationOverlayProvider(requireContext())
                    .compositeOverlayWithThemeSurfaceColorIfNeeded(12f)
            )
        }

        setupNavigation()
        setupOptionsMenu()

        viewModel.item.observe(
            viewLifecycleOwner,
            Observer { item: Item? ->
                if (item != null) {
                    val imgUrl = httpToHttps(item.img)
                    Picasso.get()
                        .load(imgUrl)
                        .fit()
                        .centerCrop(Gravity.TOP)
                        .error(R.drawable.ic_baseline_error_outline_24)
                        .into(viewDataBinding?.imageView)

                    viewDataBinding?.collapsingToolbar?.title = item.localName ?: item.name

                    viewDataBinding?.daysObserving?.count?.text =
                        millisToDays(item.observingTimeInMs).toString()
                    viewDataBinding?.updatingTime?.count?.text =
                        SimpleDateFormat(
                            "dd.MM HH:mm",
                            Locale("en")
                        ).format(item.lastUpdateTimestamp)

                    viewDataBinding?.sizesLayout?.removeAllViews()
                    item.sizes.forEach {
                        val cardSizeLayoutBinding = DataBindingUtil.inflate<CardSizeLayoutBinding>(
                            layoutInflater,
                            R.layout.card_size_layout,
                            viewDataBinding?.sizesLayout,
                            true
                        )

                        cardSizeLayoutBinding.size = it
                        val storeIds = it.storesWithQuantity
                        if (storeIds == null) {
                            cardSizeLayoutBinding.warehousesInfo.visibility = View.GONE
                        } else {
                            cardSizeLayoutBinding.warehousesInfo.visibility = View.VISIBLE
                            cardSizeLayoutBinding.count5.text =
                                storeIds
                        }
                    }
                }
            }
        )

        viewDataBinding?.swipeRefresh?.setOnRefreshListener {
            viewModel.refreshItem()
        }

        viewModel.dataLoading.observe(
            viewLifecycleOwner,
            Observer {
                viewDataBinding?.swipeRefresh?.isRefreshing = it
            }
        )

        viewDataBinding?.ordersCount?.apply {
            icon.setImageResource(R.drawable.ic_baseline_bar_chart_24)
            root.setOnClickListener {
                viewModel.showOrdersChart()
            }
        }

        postponeEnterTransition()
        view?.doOnPreDraw { startPostponedEnterTransition() }
    }

    private fun setupOptionsMenu() {
        viewDataBinding?.toolbar?.setOnMenuItemClickListener { item ->
            when (item?.itemId) {
                R.id.menu_delete -> {
                    viewModel.confirmDelete()
                    true
                }
                R.id.menu_edit -> {
                    viewModel.editItem()
                    true
                }
                R.id.menu_refresh -> {
                    viewModel.refreshItem()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupNavigation() {

        viewModel.confirmDeleteEvent.observe(
            viewLifecycleOwner,
            EventObserver {
                val action = DetailFragmentDirections
                    .actionDetailFragmentToConfirmRemoveDialogFragment2(arrayOf(it))
                navigate(action)
            }
        )

        viewModel.editItemEvent.observe(
            viewLifecycleOwner,
            EventObserver {
                exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
                reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
                val action = DetailFragmentDirections.actionDetailFragmentToEditItemFragment(it)
                navigate(action)
            }
        )

        viewModel.updateErrorEvent.observe(
            viewLifecycleOwner,
            EventObserver {
                val action = DetailFragmentDirections.actionDetailFragmentToErrorDialogFragment(it)
                navigate(action)
            }
        )

        viewModel.showOrdersChartEvent.observe(
            viewLifecycleOwner,
            EventObserver {
                exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
                reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
                val action = DetailFragmentDirections.actionDetailFragmentToChartFragment(it)
                navigate(action)
            }
        )
    }
}
