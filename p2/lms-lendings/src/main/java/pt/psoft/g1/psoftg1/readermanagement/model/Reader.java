package pt.psoft.g1.psoftg1.readermanagement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Reader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Column(unique = true) 
    private String readerNumber;

    private String name; 

    public Reader(String readerNumber) {
        this.readerNumber = readerNumber;
    }

    public Reader(String readerNumber, String name) { 
        this.readerNumber = readerNumber;
        this.name = name;
    }
}