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
	sub	$29,$29,32		; allocate frame
	stw	$25,$29,8		; save old frame pointer
	add	$25,$29,32		; setup new frame pointer
	stw	$31,$25,-28		; save return register
	add	$8,$25,-16
	add	$9,$0,0
	add	$10,$0,4
	bgeu	$9,$10,_indexError
	mul	$9,$9,4
	add	$8,$8,$9
	add	$9,$0,3
	stw	$9,$8,0
	add	$8,$25,-16
	add	$9,$0,1
	add	$10,$0,4
	bgeu	$9,$10,_indexError
	mul	$9,$9,4
	add	$8,$8,$9
	add	$9,$0,2
	stw	$9,$8,0
	add	$8,$25,-16
	add	$9,$0,2
	add	$10,$0,4
	bgeu	$9,$10,_indexError
	mul	$9,$9,4
	add	$8,$8,$9
	add	$9,$0,4
	stw	$9,$8,0
	add	$8,$25,-16
	add	$9,$0,3
	add	$10,$0,4
	bgeu	$9,$10,_indexError
	mul	$9,$9,4
	add	$8,$8,$9
	add	$9,$0,1
	stw	$9,$8,0
	add	$8,$25,-16
	stw	$8,$29,0		; store arg #0
	jal	sort
	add	$8,$25,-20
	add	$9,$0,0
	stw	$9,$8,0
L0:
	add	$8,$25,-20
	ldw	$8,$8,0
	add	$9,$0,4
	bge	$8,$9,L1
	add	$8,$25,-16
	add	$9,$25,-20
	ldw	$9,$9,0
	add	$10,$0,4
	bgeu	$9,$10,_indexError
	mul	$9,$9,4
	add	$8,$8,$9
	ldw	$8,$8,0
	stw	$8,$29,0		; store arg #0
	jal	printi
	add	$8,$25,-20
	add	$9,$25,-20
	ldw	$9,$9,0
	add	$10,$0,1
	add	$9,$9,$10
	stw	$9,$8,0
	j	L0
L1:
	add	$8,$0,10
	stw	$8,$29,0		; store arg #0
	jal	printc
	ldw	$31,$25,-28		; restore return register
	ldw	$25,$29,8		; restore old frame pointer
	add	$29,$29,32		; release frame
	jr	$31			; return

	.export	sort
sort:
	sub	$29,$29,20		; allocate frame
	stw	$25,$29,0		; save old frame pointer
	add	$25,$29,20		; setup new frame pointer
	add	$8,$25,-8
	add	$9,$0,0
	stw	$9,$8,0
L2:
	add	$8,$25,-8
	ldw	$8,$8,0
	add	$9,$0,3
	bge	$8,$9,L3
	add	$8,$25,-4
	add	$9,$0,0
	stw	$9,$8,0
	add	$8,$25,-8
	add	$9,$0,0
	stw	$9,$8,0
L4:
	add	$8,$25,-4
	ldw	$8,$8,0
	add	$9,$0,3
	bge	$8,$9,L5
	add	$8,$25,0
	ldw	$8,$8,0
	add	$9,$25,-4
	ldw	$9,$9,0
	add	$10,$0,4
	bgeu	$9,$10,_indexError
	mul	$9,$9,4
	add	$8,$8,$9
	ldw	$8,$8,0
	add	$9,$25,0
	ldw	$9,$9,0
	add	$10,$25,-4
	ldw	$10,$10,0
	add	$11,$0,1
	add	$10,$10,$11
	add	$11,$0,4
	bgeu	$10,$11,_indexError
	mul	$10,$10,4
	add	$9,$9,$10
	ldw	$9,$9,0
	ble	$8,$9,L6
	add	$8,$25,-12
	add	$9,$25,0
	ldw	$9,$9,0
	add	$10,$25,-4
	ldw	$10,$10,0
	add	$11,$0,4
	bgeu	$10,$11,_indexError
	mul	$10,$10,4
	add	$9,$9,$10
	ldw	$9,$9,0
	stw	$9,$8,0
	add	$8,$25,-16
	add	$9,$25,0
	ldw	$9,$9,0
	add	$10,$25,-4
	ldw	$10,$10,0
	add	$11,$0,1
	add	$10,$10,$11
	add	$11,$0,4
	bgeu	$10,$11,_indexError
	mul	$10,$10,4
	add	$9,$9,$10
	ldw	$9,$9,0
	stw	$9,$8,0
	add	$8,$25,0
	ldw	$8,$8,0
	add	$9,$25,-4
	ldw	$9,$9,0
	add	$10,$0,4
	bgeu	$9,$10,_indexError
	mul	$9,$9,4
	add	$8,$8,$9
	add	$9,$25,-16
	ldw	$9,$9,0
	stw	$9,$8,0
	add	$8,$25,0
	ldw	$8,$8,0
	add	$9,$25,-4
	ldw	$9,$9,0
	add	$10,$0,1
	add	$9,$9,$10
	add	$10,$0,4
	bgeu	$9,$10,_indexError
	mul	$9,$9,4
	add	$8,$8,$9
	add	$9,$25,-12
	ldw	$9,$9,0
	stw	$9,$8,0
	j	L7
L6:
	add	$8,$25,-8
	add	$9,$25,-8
	ldw	$9,$9,0
	add	$10,$0,1
	add	$9,$9,$10
	stw	$9,$8,0
L7:
	add	$8,$25,-4
	add	$9,$25,-4
	ldw	$9,$9,0
	add	$10,$0,1
	add	$9,$9,$10
	stw	$9,$8,0
	j	L4
L5:
	j	L2
L3:
	ldw	$25,$29,0		; restore old frame pointer
	add	$29,$29,20		; release frame
	jr	$31			; return
