package org.shuavideos.service.video.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.shuavideos.entity.video.Type;
import org.shuavideos.mapper.video.TypeMapper;
import org.shuavideos.service.video.TypeService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class TypeServiceImpl extends ServiceImpl<TypeMapper, Type> implements TypeService {

    @Override
    public List<String> getLabels(Long typeId) {
        return Collections.emptyList();
    }

    @Override
    public List<String> random10Labels() {
        final List<Type> types = list(null);
        Collections.shuffle(types);
        final ArrayList<String> labels = new ArrayList<>();
        for (Type type : types) {
            for (String label : type.buildLabel()) {
                if (labels.size() == 10){
                    return labels;
                }
                labels.add(label);
            }
        }
        return labels;
    }
}
