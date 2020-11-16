	.import	printi
	.import	printc
	.import	readi
	.import	readc
	.import	exit
	.import	time
	.import	clearAll
	.import	setPixel
	.import	drawLine
	.import	drawCircle
	.import	_indexError

	.code
	.align	4

	.export	main
main:
	sub	$29,$29,24		; allocate frame
	stw	$25,$29,12		; save old frame pointer
	add	$25,$29,24		; setup new frame pointer
	stw	$31,$25,-16		; save return register
	add	$8,$25,-4
	add	$9,$0,4
	stw	$9,$8,0
	add	$8,$25,-8
	add	$9,$0,3
	stw	$9,$8,0
	add	$8,$25,-4
	ldw	$8,$8,0
	stw	$8,$29,0		; store arg #0
	add	$8,$25,-8
	ldw	$8,$8,0
	stw	$8,$29,4		; store arg #1
	jal	clac
	add	$8,$0,10
	stw	$8,$29,0		; store arg #0
	jal	printc
	ldw	$31,$25,-16		; restore return register
	ldw	$25,$29,12		; restore old frame pointer
	add	$29,$29,24		; release frame
	jr	$31			; return

	.export	clac
clac:
	sub	$29,$29,16		; allocate frame
	stw	$25,$29,8		; save old frame pointer
	add	$25,$29,16		; setup new frame pointer
	stw	$31,$25,-12		; save return register
	add	$8,$25,-4
	add	$9,$25,0
	ldw	$9,$9,0
	add	$10,$25,4
	ldw	$10,$10,0
	add	$9,$9,$10
	stw	$9,$8,0
	add	$8,$25,-4
	ldw	$8,$8,0
	stw	$8,$29,0		; store arg #0
	jal	printi
	ldw	$31,$25,-12		; restore return register
	ldw	$25,$29,8		; restore old frame pointer
	add	$29,$29,16		; release frame
	jr	$31			; return
