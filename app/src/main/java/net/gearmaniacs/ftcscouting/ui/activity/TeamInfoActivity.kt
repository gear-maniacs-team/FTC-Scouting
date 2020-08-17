package net.gearmaniacs.ftcscouting.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.extensions.longToast
import net.gearmaniacs.core.extensions.textString
import net.gearmaniacs.core.extensions.toIntOrDefault
import net.gearmaniacs.core.model.UserData
import net.gearmaniacs.core.model.isNullOrEmpty
import net.gearmaniacs.ftcscouting.R
import net.gearmaniacs.ftcscouting.databinding.TeamInfoActivityBinding
import net.gearmaniacs.ftcscouting.viewmodel.TeamInfoViewModel

@AndroidEntryPoint
class TeamInfoActivity : AppCompatActivity() {

    private val viewModel by viewModels<TeamInfoViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = TeamInfoActivityBinding.inflate(layoutInflater)
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
            val number = binding.etTeamNumber.textString.toIntOrDefault(-1)
            val teamName = binding.etTeamName.textString

            if (number < 0) {
                binding.etTeamNumber.error = getString(R.string.error_invalid_team_number)
                return@setOnClickListener
            }

            if (teamName.isEmpty()) {
                binding.etTeamName.error = getString(R.string.error_invalid_team_name)
                return@setOnClickListener
            }

            val newUserData = UserData(number, teamName)
            viewModel.updateUserData(newUserData)
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
