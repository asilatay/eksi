package service;

import java.util.Comparator;

import model.UserUserTitle;

public class ComparatorClassForTitleSize implements Comparator<UserUserTitle>{
 
    @Override
    public int compare(UserUserTitle e1, UserUserTitle e2) {
        if(e1.getCountOfSimilarTitle() < e2.getCountOfSimilarTitle()){
            return 1;
        } else {
            return -1;
        }
    }
}
