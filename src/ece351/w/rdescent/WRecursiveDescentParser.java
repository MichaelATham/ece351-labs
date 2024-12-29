/* *********************************************************************
 * ECE351 
 * Department of Electrical and Computer Engineering 
 * University of Waterloo 
 * Term: Fall 2021 (1219)
 *
 * The base version of this file is the intellectual property of the
 * University of Waterloo. Redistribution is prohibited.
 *
 * By pushing changes to this file I affirm that I am the author of
 * all changes. I affirm that I have complied with the course
 * collaboration policy and have not plagiarized my work. 
 *
 * I understand that redistributing this file might expose me to
 * disciplinary action under UW Policy 71. I understand that Policy 71
 * allows for retroactive modification of my final grade in a course.
 * For example, if I post my solutions to these labs on GitHub after I
 * finish ECE351, and a future student plagiarizes them, then I too
 * could be found guilty of plagiarism. Consequently, my final grade
 * in ECE351 could be retroactively lowered. This might require that I
 * repeat ECE351, which in turn might delay my graduation.
 *
 * https://uwaterloo.ca/secretariat-general-counsel/policies-procedures-guidelines/policy-71
 * 
 * ********************************************************************/

package ece351.w.rdescent;

import org.parboiled.common.ImmutableList;

import ece351.util.Lexer;
import ece351.w.ast.WProgram;
import ece351.w.ast.Waveform;

public final class WRecursiveDescentParser {

    private String state = "ID";
    private int bits = 0;

    WProgram program = new WProgram();  // Initializing a WProgram object that will hold the final WProgram returned by parse
    Waveform wf = new Waveform();       // A "dummy" wf thats initialized for the use of adding to the WProgram's immutable list of waveforms

    private final Lexer lexer;

    public WRecursiveDescentParser(final Lexer lexer) {
        this.lexer = lexer;
    }

    public static WProgram parse(final String input) {
    	final WRecursiveDescentParser p = new WRecursiveDescentParser(new Lexer(input));
        return p.parse();
    }

    public WProgram parse() {
        program = new WProgram();   // Just to be safe, ensuring when parse is called that program is set to a new program (empty waveform list)
        while(!lexer.inspectEOF()) {
            waveform();
        } lexer.consumeEOF();
        return program;
    }


    public void waveform() { // Same as in WRecursiveDescentRecognizer.java
        switch(state) {
            case "ID":
                ID();
                break;
            
            case "colon":
                colon();
                break;

            case "bits":
                bits();
                break;
        }
    }
    
    public void ID() {
        if(lexer.inspectID()) {
            wf = wf.rename(lexer.consumeID()); // Instead of just consuming, we use the returned ID from lexer.consumeID() to rename the waveform to its proper name
            state = "colon";
        } else throw new IllegalArgumentException();
        return;
    }

    public void colon() {
        if(lexer.inspect(":")) {
            lexer.consume(":"); // No need to store this colon anywhere. It just needs to be checked to ensure the valid waveform format
            state = "bits";
        } else throw new IllegalArgumentException();
        return;
    }

    public void bits() {
        if(lexer.inspect("0")) {                // If the next token is 0
            wf = wf.append(lexer.consume("0")); // Add 0 to the immutable list of bits using the Waveform.append helper
            bits++;                               // Increase bits by 1
            if(lexer.inspectEOF()) {
                throw new IllegalArgumentException();
            }
            return;
        } else if(lexer.inspect("1")) {         // If the next token is 1
            wf = wf.append(lexer.consume("1")); // Add 1 to the immutable list of bits using the Waveform.append helper
            bits++;                               // Increase bits by 1
            if(lexer.inspectEOF()) {
                throw new IllegalArgumentException();
            }
            return;
        } else if(lexer.inspect(";")) {
            if(bits > 0) {                        // Same as in WRecursiveDescentRecognizer.java
                lexer.consume(";");             // No need to consume into any part of the Waveform object
                program = program.append(wf);     // Since the current waveform has been validated, add it to the list of waveforms in the WProgram object
                wf = new Waveform();              // Reset wf with its default constructor for the next waveform to repopulate its data
                bits = 0;
                state = "ID";                     
                return;
            } else throw new IllegalArgumentException();
        } else throw new IllegalArgumentException();
    }

}
