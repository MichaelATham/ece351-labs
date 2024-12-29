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

import ece351.util.Lexer;

public final class WRecursiveDescentRecognizer {

    private String state = "ID";    // For this lab I used a string that I treat as a state variable to keep track of what the lexer should be looking for
    private int bits = 0;           // Keeps track of the number of bits the current waveform has to ensure a waveform has at least 1 bit for each ID

    private final Lexer lexer;

    public WRecursiveDescentRecognizer(final Lexer lexer) {
        this.lexer = lexer;
    }

    public static void recognize(final String input) {
    	final WRecursiveDescentRecognizer r = new WRecursiveDescentRecognizer(new Lexer(input));
        r.recognize();
    }

    /**
     * Throws an exception to reject.
     */
    public void recognize() {
        program();
    }

    /**
     * What is the termination condition of the loop in program()?
     * Will this condition be met if the waveform() method does nothing?
     */
    public void program() {
        waveform();
        while (!lexer.inspectEOF()) {
            waveform();
        }
        lexer.consumeEOF();
    }

    public void waveform() {
        switch(state) {     // A case statement used to properly direct the lexer to what it should be inspecting/consuming
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
        if(lexer.inspectID()) { // Verifies that there is an ID present
            lexer.consumeID();  // Consume's the ID
            state = "colon";
        } else throw new IllegalArgumentException(); // If there is not an ID present that follows the waveform regex, throws exception
        return;
    }

    public void colon() {
        if(lexer.inspect(":")) { // Verifies the next token after the ID is a colon
            lexer.consume(":");
            state = "bits";
        } else throw new IllegalArgumentException(); // If no colon is present after the ID, there is an invalid token => throw exception
        return;
    }

    public void bits() {
        if(lexer.inspect("0")) { // Checks if the next token is a 0
            lexer.consume("0");  // Consume the 0
            bits++;                // Increase number of bits for current ID by 1
            if(lexer.inspectEOF()) {    // If the file ends after the token, there is no semicolon => reject waveform
                throw new IllegalArgumentException();
            }
            return;
        } else if(lexer.inspect("1")) { // Same as checking if the next token is 0 but for 1
            lexer.consume("1");
            bits++;
            if(lexer.inspectEOF()) {
                throw new IllegalArgumentException();
            }
            return;
        } else if(lexer.inspect(";")) { // Checks if the next token is a semi-colon
            if(bits > 0) {                // The next token can only be a semi-colon if there were more than 0 bits present
                lexer.consume(";");
                bits = 0;                 // Resets the bits to 0 for the next ID
                state = "ID";             // Change state back to ID so lexer can continue to consume if there are more lines
                return;
            } else throw new IllegalArgumentException(); // If there is a semi-colon but there were 0 bits present throw exception (since it's an invalid waveform)
        } else throw new IllegalArgumentException();     // Anything but 0, 1, or ; token during the bit state is invalid
    }
}
