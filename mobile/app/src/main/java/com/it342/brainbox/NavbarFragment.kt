package com.it342.brainbox

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.it342.brainbox.auth.LoginActivity
import com.it342.brainbox.auth.ProfileActivity
import com.it342.brainbox.network.SessionManager

class NavbarFragment : Fragment() {

    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sessionManager = SessionManager(requireContext())
        return inflater.inflate(R.layout.fragment_navbar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profileSection: LinearLayout = view.findViewById(R.id.profile_section)
        profileSection.setOnClickListener {
            showProfileMenu(it)
        }
        initUsername(view)
    }

    private fun initUsername(view: View) {
        val usernameText: TextView = view.findViewById(R.id.username_text) ?: return
        val username = sessionManager.fetchUsername() ?: "User"
        usernameText.text = username
    }

    private fun showProfileMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menu.add("View Profile")
        popup.menu.add("Logout")
        
        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "View Profile" -> {
                    startActivity(Intent(requireContext(), ProfileActivity::class.java))
                    true
                }
                "Logout" -> {
                    val modal = ModalFragment.newInstance(
                        title = "Logout",
                        message = "Are you sure you want to log out?",
                        positiveText = "Logout",
                        negativeText = "Cancel"
                    )
                    modal.setPositiveListener {
                        sessionManager.clearSession()
                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        requireActivity().finish()
                    }
                    modal.show(parentFragmentManager, "LogoutModal")
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}
