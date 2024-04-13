package com.stefan.universe.ui.auth.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.stefan.universe.common.EventObserver
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.base.BaseFragment
import com.stefan.universe.databinding.FragmentAdditionalDetailsBinding
import com.stefan.universe.ui.main.ui.UniApplication
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdditionalDetailsFragment :
    BaseFragment<FragmentAdditionalDetailsBinding, AdditionalDetailsViewModel>() {
    override val viewModel: AdditionalDetailsViewModel by viewModels()

    private var imageUri: Uri? = null

    override fun getViewBinding(container: ViewGroup?): FragmentAdditionalDetailsBinding {
        return FragmentAdditionalDetailsBinding.inflate(layoutInflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as UniApplication).resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    imageUri = result.data?.data
                    binding.profilePicture.setImageURI(result.data?.data)
                }
            }
        binding.saveButton.setOnClickListener {
            viewModel.action(
                AdditionalDetailsUserIntent.SaveAdditionalInformation(
                    binding.completeNameText.text?.toString(),
                    imageUri
                )
            )
        }
        binding.profilePicture.setOnClickListener { openGallery() }
        binding.additionalDetailsSkipButton.setOnClickListener {
            viewModel.action(
                AdditionalDetailsUserIntent.Skip
            )
        }
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
            binding.loadingLayout.visibility = if (it.loading) View.VISIBLE else View.GONE
        }
        viewModel.sideEffect.observe(this, sideEffectsObserver)
        viewModel.uiState.observe(this, uiStateObserver)
    }

    override fun handleSideEffects(sideEffect: SideEffect) {
        when (sideEffect) {
            is AdditionalDetailsSideEffect.NavigateToMain -> {
                val action =
                    AdditionalDetailsFragmentDirections.actionAdditionalDetailsFragmentToHomeFragment()
                findNavController().navigate(action)

            }

            is AdditionalDetailsSideEffect.Feedback -> {
                Toast.makeText(requireContext(), sideEffect.msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}