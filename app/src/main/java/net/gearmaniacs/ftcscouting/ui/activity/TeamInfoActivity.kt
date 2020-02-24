package net.gearmaniacs.ftcscouting.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import net.gearmaniacs.core.extensions.getTextString
import net.gearmaniacs.core.extensions.longToast
import net.gearmaniacs.core.extensions.toIntOrDefault
import net.gearmaniacs.core.extensions.toast
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.model.User
import net.gearmaniacs.ftcscouting.R
import net.gearmaniacs.ftcscouting.databinding.ActivityTeamInfoBinding

class TeamInfoActivity : AppCompatActivity() {

    companion object {
        private const val ARG_USER = "user"

        fun startActivity(context: Context, user: User?) {
            val intent = Intent(context, TeamInfoActivity::class.java)
            intent.putExtra(ARG_USER, user)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityTeamInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.bottomAppBar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val user = intent.getParcelableExtra<User>(ARG_USER)

        if (user != null) {
            binding.etTeamNumber.setText(user.id.toString())
            binding.etTeamName.setText(user.teamName)
        } else {
            longToast(R.string.team_info_previous_not_found)
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

            val appContext = applicationContext
            FirebaseDatabase.getInstance()
                .getReference(DatabasePaths.KEY_USERS)
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .setValue(user)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        appContext.toast(R.string.team_updated)
                    } else {
                        appContext.toast(R.string.team_update_error)
                    }
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
}
