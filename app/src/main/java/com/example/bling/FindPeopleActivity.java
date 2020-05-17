package com.example.bling;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FindPeopleActivity extends AppCompatActivity {
    private EditText searchET;
    private RecyclerView findFriendsList;
    private  String str="";
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people);

        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        searchET=findViewById(R.id.search_user_text);
        findFriendsList=findViewById(R.id.find_friends_list);
        findFriendsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSeq, int start, int before, int count) {
                if(searchET.getText().toString().equals(""))
                {
                    Toast.makeText(FindPeopleActivity.this, "Please write a name in order to search", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    str=charSeq.toString();
                    onStart();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options=null;
        if (str.equals(""))
        {
            options=
                    new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(userRef,Contacts.class)
                    .build();
        }
        else
        {
            options=
                    new FirebaseRecyclerOptions.Builder<Contacts>()
                            .setQuery(userRef.orderByChild("name")
                                            .startAt(str)
                                            .endAt(str+"\uf8ff")
                                    ,Contacts.class)
                            .build();

        }
        FirebaseRecyclerAdapter<Contacts,FindFriendsViewHolder> firebaseRecyclerAdapter
                =new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder,final int position, @NonNull final Contacts model)
            {
                holder.userNameTV.setText(model.getName());
                Picasso.get().load(model.getImage()).into(holder.profileIV);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        String visit_user_id=getRef(position).getKey();
                        Intent intent=new Intent(FindPeopleActivity.this,ProfileActivity.class);
                        intent.putExtra("visit_user_id",visit_user_id);
                        intent.putExtra("profile_image",model.getImage());
                        intent.putExtra("profile_name",model.getName());
                        startActivity(intent);

                    }
                });

            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup p, int viewType) {
                View view = LayoutInflater.from(p.getContext()).inflate(R.layout.contact_design,p,false);
                FindFriendsViewHolder viewHolder=new FindFriendsViewHolder(view);
                return viewHolder;

            }
        };
        findFriendsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userNameTV;
        Button videoCallBtn;
        ImageView profileIV;
        RelativeLayout cardView;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTV=itemView.findViewById(R.id.name_contact);
            videoCallBtn=itemView.findViewById(R.id.call_btn);
            profileIV=itemView.findViewById(R.id.image_contact);
            cardView=itemView.findViewById(R.id.card_view1);

            videoCallBtn.setVisibility(View.GONE);
        }

    }
}