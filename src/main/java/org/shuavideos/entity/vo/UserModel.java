package org.shuavideos.entity.vo;

import lombok.Data;
import org.shuavideos.holder.UserHolder;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserModel {
    private List<Model> models;
    private Long userId;




    public static UserModel buildUserModel(List<String> labels,Long videoId,Double score){
        final UserModel userModel = new UserModel();
        final ArrayList<Model> models = new ArrayList<>();
        userModel.setUserId(UserHolder.get());
        for (String label : labels) {
            final Model model = new Model();
            model.setLabel(label);
            model.setScore(score);
            model.setVideoId(videoId);
            models.add(model);
        }
        userModel.setModels(models);
        return userModel;
    }

}
