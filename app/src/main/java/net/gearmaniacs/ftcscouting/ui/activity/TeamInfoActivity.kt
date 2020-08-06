package net.gearmaniacs.ftcscouting.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.extensions.getTextString
import net.gearmaniacs.core.extensions.longToast
import net.gearmaniacs.core.extensions.toIntOrDefault
import net.gearmaniacs.core.extensions.toast
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.model.UserData
import net.gearmaniacs.core.model.isNullOrEmpty
import net.gearmaniacs.core.utils.AppPreferences
import net.gearmaniacs.ftcscouting.R
import net.gearmaniacs.ftcscouting.databinding.ActivityTeamInfoBinding
import javax.inject.Inject

@AndroidEntryPoint
class TeamInfoActivity : AppCompatActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityTeamInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.bottomAppBar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val originalUserData = intent.getParcelableExtra<UserData>(ARG_USER)

        if (originalUserData.isNullOrEmpty()) {
            longToast(R.string.team_info_previous_not_found)
        } else {
            binding.etTeamNumber.setText(originalUserData.id.toString())
            binding.etTeamName.setText(originalUserData.teamName)
        }

        binding.btnUpdateAccount.setOnClickListener {
            val number = binding.etTeamNumber.getTextString().toIntOrDefault(-1)
            val teamName = binding.etTeamName.getTextString()

            if (number < 0) {
                binding.etTeamNumber.error = getString(R.string.error_invalid_team_number)
                return@setOnClickListener
            }

            if (teamName.isEmpty()) {
                binding.etTeamName.error = getString(R.string.error_invalid_team_name)
                return@setOnClickListener
            }

            val newUserData = UserData(number, teamName)

            appPreferences.userDataNumber.set(newUserData.id)
            appPreferences.userDataName.set(newUserData.teamName)

            // TODO: Move to ViewModel and repo
            val appContext = applicationContext
            if (Firebase.isLoggedIn) {
                FirebaseDatabase.getInstance()
                    .getReference(DatabasePaths.KEY_USERS)
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .setValue(newUserData)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            appContext.toast(R.string.team_updated)
                        } else {
                            appContext.toast(R.string.team_update_error)
                        }
                    }
            } else {
                appContext.toast(R.string.team_updated)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val ARG_USER = "user"

        fun startActivity(context: Context, userData: UserData?) {
            val intent = Intent(context, TeamInfoActivity::class.java)
            intent.putExtra(ARG_USER, userData)
            context.startActivity(intent)
        }
    }
}
