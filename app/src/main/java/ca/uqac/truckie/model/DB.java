package ca.uqac.truckie.model;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import ca.uqac.truckie.MyUser;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import durdinapps.rxfirebase2.RxFirebaseAuth;
import durdinapps.rxfirebase2.RxFirebaseChildEvent;
import durdinapps.rxfirebase2.RxFirebaseDatabase;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class DB {

    private static final String USERS_TABLE = "users";
    private static final String DELIVERIES_TABLE = "deliveries";

    private static DB instance;
    private DatabaseReference mDatabase;

    private DB(){
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public static DB getInstance() {
        if(instance == null){
            instance = new DB();
        }
        return instance;
    }

    @SuppressLint("CheckResult")
    public void auth(String email, String password, Consumer<? super Boolean> onSuccess, Consumer<? super Throwable> onError){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        RxFirebaseAuth.signInWithEmailAndPassword(auth, email, password)
                .map(authResult -> authResult.getUser() != null)
                .subscribe(onSuccess, onError);
    }

    public void addUser(UserEntity user, OnCompleteListener listener) {
        mDatabase.child(USERS_TABLE).child(user.getId()).setValue(user).addOnCompleteListener(listener);
    }

    public void addDelivery(final DeliveryEntity delivery, final OnCompleteListener listener){
        DatabaseReference ref = mDatabase.child(DELIVERIES_TABLE + "/_lastID");
        ref.runTransaction(new Transaction.Handler() {

            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Long lastID = mutableData.getValue(Long.class);
                if (lastID == null) {
                    mutableData.setValue(1);
                }
                else {
                    mutableData.setValue(lastID + 1);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    Long id = dataSnapshot.getValue(Long.class);
                    if(id != null) {
                        delivery.setId(id);
                        mDatabase.child(DELIVERIES_TABLE).child(String.valueOf(id)).setValue(delivery).addOnCompleteListener(listener);
                    }
                }
            }
        });
    }
}
