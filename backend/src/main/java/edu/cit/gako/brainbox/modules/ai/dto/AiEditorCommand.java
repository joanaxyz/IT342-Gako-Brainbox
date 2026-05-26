package edu.cit.gako.brainbox.modules.ai.dto;

import lombok.Data;

@Data
public class AiEditorCommand {
    private String name;
    private String value;
    private Integer level;
    private Integer rows;
    private Integer cols;
    private Boolean withHeaderRow;
    private String href;
    private String latex;
}
