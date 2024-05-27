declare i32 @getint()
declare void @putint(i32)
declare void @putch(i32)
declare void @putstr(i8*)


define dso_local i32 @main(){

ain_entry:
	%v11 = alloca i32
	%v12 = call i32 @getint()
	store i32 %v12, i32* %v11
	%v13 = alloca i32
	%v14 = call i32 @getint()
	store i32 %v14, i32* %v13
	%v15 = load i32, i32* %v11
	%v16 = load i32, i32* %v13
	%v17 = and i32 %v15,%v16
	call void @putint(i32 %v17)
	ret i32 0
}
