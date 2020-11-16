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
	add	$9,$0,5
	stw	$9,$8,0
	add	$8,$25,-8
	add	$9,$0,1
	stw	$9,$8,0
	add	$8,$25,-4
	ldw	$8,$8,0
	stw	$8,$29,0		; store arg #0
	jal	facu
	add	$8,$0,10
	stw	$8,$29,0		; store arg #0
	jal	printc
	add	$8,$25,-4
	ldw	$8,$8,0
	stw	$8,$29,0		; store arg #0
	add	$8,$25,-8
	ldw	$8,$8,0
	stw	$8,$29,4		; store arg #1
	jal	facur
	add	$8,$0,10
	stw	$8,$29,0		; store arg #0
	jal	printc
	ldw	$31,$25,-16		; restore return register
	ldw	$25,$29,12		; restore old frame pointer
	add	$29,$29,24		; release frame
	jr	$31			; return

	.export	facu
facu:
	sub	$29,$29,16		; allocate frame
	stw	$25,$29,8		; save old frame pointer
	add	$25,$29,16		; setup new frame pointer
	stw	$31,$25,-12		; save return register
	add	$8,$25,-4
	add	$9,$0,1
	stw	$9,$8,0
L0:
	add	$8,$25,0
	ldw	$8,$8,0
	add	$9,$0,1
	ble	$8,$9,L1
	add	$8,$25,-4
	add	$9,$25,-4
	ldw	$9,$9,0
	add	$10,$25,0
	ldw	$10,$10,0
	mul	$9,$9,$10
	stw	$9,$8,0
	add	$8,$25,0
	add	$9,$25,0
	ldw	$9,$9,0
	add	$10,$0,1
	sub	$9,$9,$10
	stw	$9,$8,0
	j	L0
L1:
	add	$8,$25,-4
	ldw	$8,$8,0
	stw	$8,$29,0		; store arg #0
	jal	printi
	ldw	$31,$25,-12		; restore return register
	ldw	$25,$29,8		; restore old frame pointer
	add	$29,$29,16		; release frame
	jr	$31			; return

	.export	facur
facur:
	sub	$29,$29,16		; allocate frame
	stw	$25,$29,12		; save old frame pointer
	add	$25,$29,16		; setup new frame pointer
	stw	$31,$25,-8		; save return register
	add	$8,$25,0
	ldw	$8,$8,0
	add	$9,$0,1
	ble	$8,$9,L2
	add	$8,$25,4
	add	$9,$25,4
	ldw	$9,$9,0
	add	$10,$25,0
	ldw	$10,$10,0
	mul	$9,$9,$10
	stw	$9,$8,0
	add	$8,$25,0
	add	$9,$25,0
	ldw	$9,$9,0
	add	$10,$0,1
	sub	$9,$9,$10
	stw	$9,$8,0
	add	$8,$25,0
	ldw	$8,$8,0
	stw	$8,$29,0		; store arg #0
	add	$8,$25,4
	ldw	$8,$8,0
	stw	$8,$29,4		; store arg #1
	jal	facur
	j	L3
L2:
	add	$8,$25,4
	ldw	$8,$8,0
	stw	$8,$29,0		; store arg #0
	jal	printi
L3:
	ldw	$31,$25,-8		; restore return register
	ldw	$25,$29,12		; restore old frame pointer
	add	$29,$29,16		; release frame
	jr	$31			; return
