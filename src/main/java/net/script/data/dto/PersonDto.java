package net.script.data.dto;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.LongProperty;
import javafx.beans.property.StringProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonDto extends RecursiveTreeObject<PersonDto> {
    private LongProperty id;
    private StringProperty name;
    private StringProperty lastName;
}
