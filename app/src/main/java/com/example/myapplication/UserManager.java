package com.example.myapplication;

import java.util.ArrayList;

public class UserManager {
    private static UserManager instance=new UserManager();
    private UserManager(){}
    public static UserManager getInstance(){
        return instance;
    }
    private ArrayList<User> users=new ArrayList<>();
    public User getUserByName(String username){
        for(User user:users){
            if(user.getUsername().equalsIgnoreCase(username)){
                return user;
            }
        }
        return null;
    }
    public User getUserByEmail(String email){
        for(User user:users){
            if(user.getEmail().equalsIgnoreCase(email)){
                return user;
            }
        }
        return null;
    }
    public void addUser(User user){
        users.add(user);
    }


}
