package net.gearmaniacs.ftcscouting.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_account.*
import net.gearmaniacs.ftcscouting.R
import net.gearmaniacs.core.model.User
import net.gearmaniacs.core.extensions.getTextOrEmpty
import net.gearmaniacs.core.extensions.toIntOrDefault
import net.gearmaniacs.core.extensions.toast
import net.gearmaniacs.core.firebase.DatabasePaths

class AccountActivity : AppCompatActivity() {

    companion object {
        private const val ARG_USER = "user"

        fun startActivity(context: Context, user: User) {
            val intent = Intent(context, AccountActivity::class.java)
            intent.putExtra(ARG_USER, user)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        setSupportActionBar(bottom_app_bar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val user = intent.getParcelableExtra<User>(ARG_USER) ?: throw IllegalArgumentException(ARG_USER)

        et_team_number.setText(user.id.toString())
        et_team_name.setText(user.teamName)

        btn_update_account.setOnClickListener {
            val number = et_team_number.getTextOrEmpty().toIntOrDefault(-1)
            val teamName = et_team_name.getTextOrEmpty()

            if (number == -1) {
                et_team_number.error = getString(R.string.error_invalid_team_number)
                return@setOnClickListener
            }

            if (teamName.isEmpty()) {
                et_team_name.error = getString(R.string.error_invalid_team_name)
                return@setOnClickListener
            }

            val currentUserRef = FirebaseDatabase.getInstance()
                .reference
                .child(DatabasePaths.KEY_USERS)
                .child(FirebaseAuth.getInstance().currentUser!!.uid)

            val listener = object : OnSuccessListener<Void> {
                var count = 0

                override fun onSuccess(void: Void?) {
                    count++

                    if (count == 2) {
                        toast(R.string.account_updated)
                        finish()
                    }
                }
            }

            currentUserRef.child(User::id.name).setValue(number).addOnSuccessListener(listener)
            currentUserRef.child(User::teamName.name).setValue(teamName).addOnSuccessListener(listener)
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
