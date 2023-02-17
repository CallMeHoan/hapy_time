package com.happy_time.happy_time.ddd.department;

import com.happy_time.happy_time.ddd.position.PositionView;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class DepartmentView {
    private List<Item> items;
    private Integer total_department;
    private Integer total_position;

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class Item{
        private String id;
        private String department_name;
        private List<String> position_ids; // các vị trí thuộc phòng ban
        private List<String> department_children_ids; //các phòng ban dưới cấp
        private String department_parent_id; //id của phòng ban cha (chỉ 1 )
        private List<PositionView> children_position;
    }
}
