package com.stefan.universe.ui.auth.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.stefan.universe.common.EventObserver
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.base.BaseFragment
import com.stefan.universe.common.constants.Constants
import com.stefan.universe.common.utils.DateUtils
import com.stefan.universe.databinding.FragmentAdditionalDetailsBinding
import com.stefan.universe.ui.main.ui.UniApplication
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar


@AndroidEntryPoint
class AdditionalDetailsFragment :
    BaseFragment<FragmentAdditionalDetailsBinding, AdditionalDetailsViewModel>() {
    override val viewModel: AdditionalDetailsViewModel by viewModels()

    private var imageUri: Uri? = null

    override fun getViewBinding(container: ViewGroup?): FragmentAdditionalDetailsBinding {
        return FragmentAdditionalDetailsBinding.inflate(layoutInflater, container, false)
    }

    override fun bottomNavigationVisiblity(): Int {
        return View.GONE
    }

    private fun updateUi(state: AdditionalDetailsUiState) {
        binding.loadingLayout.visibility = if (state.loading) View.VISIBLE else View.GONE
        if (state.loading) return

        binding.universityTextView.apply {
            setText(state.university?.name ?: "No university found for this email")
            isClickable = false
        }

        binding.universityFacultyDropdown.apply {
            if(state.university == null) {
                setText("No faculty found for this email")
                return@apply
            }
            setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    state.university.faculties
                )
            )
            onFocusChangeListener = View.OnFocusChangeListener { _, _ -> showDropDown() }
            doOnTextChanged { _, _, _, _ ->  error = null; clearFocus() }
        }

        binding.genderDropdown.apply {
            setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    Constants.GENDER_LIST
                )
            )
            onFocusChangeListener = View.OnFocusChangeListener { _, _ -> showDropDown() }
            doOnTextChanged { _, _, _, _ -> clearFocus() }
        }

        binding.birthDateTextView.setText(state.birthDate?.let { DateUtils.getDate(it.time) })
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        (activity as UniApplication).resultLauncher.launch(intent)
    }

    override fun observeViewModel() {
        val sideEffectsObserver = EventObserver<SideEffect> {
            handleSideEffects(it)
        }
        val uiStateObserver = Observer<AdditionalDetailsUiState> {
            updateUi(it)
        }
        viewModel.sideEffect.observe(this, sideEffectsObserver)
        viewModel.uiState.observe(this, uiStateObserver)
    }

    override fun setupListeners() {
        (activity as UniApplication).resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    imageUri = result.data?.data
                    binding.profilePicture.setImageURI(result.data?.data)
                }
            }
        binding.birthDateTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar[Calendar.YEAR]
            val month = calendar[Calendar.MONTH]
            val day = calendar[Calendar.DAY_OF_MONTH]

            val datePickerDialog =
                DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDateCalendar = Calendar.getInstance()
                    selectedDateCalendar.set(selectedYear, selectedMonth, selectedDay)
                    viewModel.action(AdditionalDetailsUserIntent.SelectBirthDate(selectedDateCalendar.time))
                }, year, month, day)

            datePickerDialog.show()
        }
        binding.saveButton.setOnClickListener {
            viewModel.action(
                AdditionalDetailsUserIntent.SaveAdditionalInformation(
                    binding.completeNameText.text?.toString(),
                    imageUri,
                    binding.universityFacultyDropdown.text?.toString(),
                    binding.genderDropdown.text?.toString(),
                )
            )
        }
        binding.profilePicture.setOnClickListener { openGallery() }
    }

    override fun handleSideEffects(sideEffect: SideEffect) {
        when (sideEffect) {
            is AdditionalDetailsSideEffect.NavigateToMain -> {
                val action =
                    AdditionalDetailsFragmentDirections.actionAdditionalDetailsFragmentToHomeFragment()
                findNavController().navigate(action)

            }

            is AdditionalDetailsSideEffect.NotifyErrors -> {
                sideEffect.mandatoryFields.forEach {
                    when (it) {
                        MandatoryFields.DISPLAY_NAME -> {
                            binding.completeNameText.error = "Please enter your complete name"
                        }
                        MandatoryFields.FACULTY -> {
                            binding.universityFacultyDropdown.error = "Please select faculty"
                        }
                    }
                }
            }

            is AdditionalDetailsSideEffect.Feedback -> {
                Toast.makeText(requireContext(), sideEffect.msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}