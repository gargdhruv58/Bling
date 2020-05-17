package com.example.bling;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ContactsActivity extends AppCompatActivity {

    BottomNavigationView navView;
    RecyclerView myContactsList;
    ImageView findPeopleBtn;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private DatabaseReference contactsRef,usersRef;
    private String userName="",profileImage="";
    private String calledBy="",glitch="";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");


        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        findPeopleBtn=findViewById(R.id.find_people_Btn);
        myContactsList=findViewById(R.id.contact_list);

        myContactsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        findPeopleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent findPeopleIntent=new Intent(ContactsActivity.this, FindPeopleActivity.class);
                startActivity(findPeopleIntent);
            }
        });



    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener=new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch(item.getItemId())
            {
                case R.id.navigation_home:
                    Intent mainIntent=new Intent(ContactsActivity.this, ContactsActivity.class);
                    startActivity(mainIntent);
                    break;
                case R.id.navigation_settings:
                    Intent settingsIntent=new Intent(ContactsActivity.this,SettingsActivity.class);
                    startActivity(settingsIntent);
                    break;
                case R.id.navigation_notifications:
                    Intent notificationIntent=new Intent(ContactsActivity.this,NotificationActivity.class);
                    startActivity(notificationIntent);
                    break;
                case R.id.navigation_logout:
                    FirebaseAuth.getInstance().signOut();
                    Intent logoutIntent=new Intent(ContactsActivity.this,RegistrationActivity.class);
                    startActivity(logoutIntent);
                    finish();
                    break;
            }
            return true;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();


        validateUser();

        checkForReceivingCall();
        glitch="true";




        FirebaseRecyclerOptions<Contacts> options
                =new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef.child(currentUserId),Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> firebaseRecyclerAdapter
                =new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int i, @NonNull Contacts contacts)
            {
                final String listUserId=getRef(i).getKey();

                usersRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            userName=dataSnapshot.child("name").getValue().toString();
                            profileImage=dataSnapshot.child("image").getValue().toString();

                            holder.userNameTV.setText(userName);
                            Picasso.get().load(profileImage).into(holder.profileIV);
                        }
                        holder.callBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                Intent callingIntent=new Intent(ContactsActivity.this,CallingActivity.class);
                                callingIntent.putExtra("visit_user_id",listUserId);
                                callingIntent.putExtra("fault",glitch);

                                startActivity(callingIntent);
                                //finish();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design,parent,false);
                ContactsViewHolder viewHolder=new ContactsViewHolder(view);
                return viewHolder;

            }
        };
        myContactsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void checkForReceivingCall()
    {
        usersRef.child(currentUserId)
                .child("Ringing")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild("ringing"))
                        {
                            glitch="false";
                            calledBy=dataSnapshot.child("ringing").getValue().toString();
                            Intent callingIntent=new Intent(ContactsActivity.this,CallingActivity.class);
                            callingIntent.putExtra("visit_user_id",calledBy);
                            //change
                            callingIntent.putExtra("fault",glitch);
                            startActivity(callingIntent);
                            //finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userNameTV;
        Button callBtn;
        ImageView profileIV;
        //RelativeLayout cardView;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTV=itemView.findViewById(R.id.name_contact);
            callBtn=itemView.findViewById(R.id.call_btn);
            profileIV=itemView.findViewById(R.id.image_contact);
            //cardView=itemView.findViewById(R.id.card_view);

        }
    }


    private void validateUser()
    {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();

        reference.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(!dataSnapshot.exists())
                        {
                            Intent settingIntent=new Intent(ContactsActivity.this,SettingsActivity.class);
                            startActivity(settingIntent);
                            finish();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
