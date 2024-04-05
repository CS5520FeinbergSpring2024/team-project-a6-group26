package edu.northeastern.group26.littlemood;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
    }

    public void signUp(View view) {
        String email = ((EditText) findViewById(R.id.SignUpEmail)).getText().toString();
        String password = ((EditText) findViewById(R.id.SignUpPassword)).getText().toString();
        String username = ((EditText) findViewById(R.id.SignUpUsername)).getText().toString();
        email="wendyjiang142322@gmail.com";
        password="Yanyan142322";
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.i("TAG", "onComplete: 44444444");
                        if (task.isSuccessful()) {
                            Log.i("TAG", "onComplete:1111 ");
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(SignUpActivity.class.getName(), "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();
                            builder.setDisplayName(username);
                            user.updateProfile(builder.build());
                            Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                            intent.putExtra("username", username);
                            startActivity(intent);
                        } else {
                            Log.i("TAG", "onComplete: 2222"+ task.getException());

                            // If sign in fails, display a message to the user.
                            Log.w(SignUpActivity.class.getName(), "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
