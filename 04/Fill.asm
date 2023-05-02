// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

//Idea : 
//Pseudo
// i = 0
// Run an infinite loop:
	// if (R[KBD] = 0)
	// 		paint pixel[i] in white
	//		if (i>0)
	//			i--
	// if (R[KBD] != 0)
	// 		paint pixel[i] in black
	//		if (i < 8191)
	//			i++

//Assembly
//init i=0
@i
M=0
(LOOP)
	@KBD
	D=M
	@PAINTB 
	D;JNE // if kbd is pressed - jump to paint black
	//paint pixel[i] in white
	@i
	D=M
	@SCREEN
	A=A+D
	M=0
	@i
	D=M
	@NOTHINGTOPAINT
	D; JEQ//if i=0 - jump to nothing to paint (white screen remains white)
	@i
	M=M-1
	@LOOP
	0;JMP

	(PAINTB) //paint pixel[i] in black
	@i
	D=M
	@SCREEN
	A=A+D
	M=-1

	@i
	D=M
	@8191
	D=D-A
	@NOTHINGTOPAINT
	D; JEQ//if i-8191=0 - jump to nothing to paint (black screen remains black)
	@i
	M=M+1
	@LOOP
	0;JMP
	
	(NOTHINGTOPAINT)
	@LOOP
	0;JMP

