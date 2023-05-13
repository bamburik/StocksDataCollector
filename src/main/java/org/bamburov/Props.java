package org.bamburov;

import lombok.Data;

@Data
public class Props {
    private String mysqlConnectionString;
    private String mysqlUsername;
    private String mysqlPassword;
    private int startPageIndex;
    private String date;
}
