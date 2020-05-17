package com.example.bling;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class CallingActivity extends AppCompatActivity
{
    private TextView nameContact;
    private ImageView profileImage;
    private ImageView cancelCallBtn,acceptCallBtn;
    private String receiverUserId="",receiverUserImage="",receiverUserName="";
    private String senderUserId="",senderUserImage="",senderUserName="",checker="";
    private String callingId="", ringingId="";
    private DatabaseReference usersRef;
    private String debugVar="";

    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        senderUserId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        receiverUserId=getIntent().getExtras().get("visit_user_id").toString();
        debugVar=getIntent().getExtras().get("fault").toString();

        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");

        mediaPlayer=MediaPlayer.create(this,R.raw.ringing);


        nameContact=findViewById(R.id.name_calling);
        profileImage=findViewById(R.id.profile_image_calling);
        cancelCallBtn=findViewById(R.id.cancel_call);
        acceptCallBtn=findViewById(R.id.make_call);

        cancelCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                checker="clicked";
                //try for a better way to do it
                cancelCallingUser();
            }
        });

        acceptCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                mediaPlayer.stop();

                final HashMap<String,Object> callingPickupMap =new HashMap<>();
                callingPickupMap.put("picked","picked");

                usersRef.child(senderUserId).child("Ringing")
                        .updateChildren(callingPickupMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
                                    Intent intent=new Intent(CallingActivity.this,VideoChatActivity.class);
                                    startActivity(intent);

                                }
                            }
                        });
            }
        });

        getAndSetUserProfileInfo();

    }


    private void getAndSetUserProfileInfo()
    {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(receiverUserId).exists())
                {
                    receiverUserImage=dataSnapshot.child(receiverUserId).child("image").getValue().toString();
                    receiverUserName=dataSnapshot.child(receiverUserId).child("name").getValue().toString();

                    nameContact.setText(receiverUserName);
                    Picasso.get().load(receiverUserImage).placeholder(R.drawable.profile_image).into(profileImage);
                }
                if(dataSnapshot.child(senderUserId).exists())
                {
                    senderUserImage=dataSnapshot.child(senderUserId).child("image").getValue().toString();
                    senderUserName=dataSnapshot.child(senderUserId).child("name").getValue().toString();

                    //nameContact.setText(senderUserName);
                    //Picasso.get().load(senderUserImage).placeholder(R.drawable.profile_image).into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onStart()
    {
        super.onStart();
        mediaPlayer.start();

       if(debugVar.equals("true"))
       {
           usersRef.child(receiverUserId)
                   .addListenerForSingleValueEvent(new ValueEventListener() {
                       @Override
                       public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                           if(!checker.equals("clicked") && !dataSnapshot.hasChild("Calling") && !dataSnapshot.hasChild("Ringing"));
                           {

                               final HashMap<String, Object> callingInfo=new HashMap<>();
                               callingInfo.put("calling",receiverUserId);

                               usersRef.child(senderUserId)
                                       .child("Calling")
                                       .updateChildren(callingInfo)
                                       .addOnCompleteListener(new OnCompleteListener<Void>() {
                                           @Override
                                           public void onComplete(@NonNull Task<Void> task) {
                                               if(task.isSuccessful())
                                               {
                                                   final HashMap<String, Object> ringingInfo=new HashMap<>();
                                                   ringingInfo.put("ringing",senderUserId);

                                                   usersRef.child(receiverUserId)
                                                           .child("Ringing")
                                                           .updateChildren(ringingInfo);
                                               }
                                           }
                                       });

                           }
                       }

                       @Override
                       public void onCancelled(@NonNull DatabaseError databaseError) {

                       }
                   });

       }
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(senderUserId).hasChild("Ringing") && !dataSnapshot.child(senderUserId).hasChild("Calling"))
                {
                    acceptCallBtn.setVisibility(View.VISIBLE);
                }

                if(dataSnapshot.child(receiverUserId).child("Ringing").hasChild("picked"))
                {
                    mediaPlayer.stop();
                    Intent intent=new Intent(CallingActivity.this,VideoChatActivity.class);
                    startActivity(intent);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void cancelCallingUser()
    {
        //from sender side
        usersRef.child(senderUserId)
                .child("Calling")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists() && dataSnapshot.hasChild("calling"))
                        {
                            callingId=dataSnapshot.child("calling").getValue().toString();

                            usersRef.child(callingId)
                                    .child("Ringing")
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                usersRef.child(senderUserId)
                                                        .child("Calling")
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                startActivity(new Intent(CallingActivity.this,RegistrationActivity.class));
                                                                finish();
                                                            }
                                                        });

                                            }
                                        }
                                    });
                        }
                        else
                        {
                            startActivity(new Intent(CallingActivity.this,RegistrationActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        //from receiver side
        usersRef.child(senderUserId)
                .child("Ringing")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists() && dataSnapshot.hasChild("ringing"))
                        {
                            callingId=dataSnapshot.child("ringing").getValue().toString();

                            usersRef.child(ringingId)
                                    .child("Calling")
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                usersRef.child(senderUserId)
                                                        .child("Ringing")
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                startActivity(new Intent(CallingActivity.this,RegistrationActivity.class));
                                                                finish();
                                                            }
                                                        });

                                            }
                                        }
                                    });
                        }
                        else
                        {
                            startActivity(new Intent(CallingActivity.this,RegistrationActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }





}
