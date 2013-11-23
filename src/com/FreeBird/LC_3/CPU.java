package com.FreeBird.LC_3;

import java.io.FileInputStream;
import android.os.Environment;
import android.util.Log;

public class CPU{

	static final int PC = 8, IR = 9, PSR = 10,
			GETC = 0x0020, OUT = 0x0021, PUTS = 0x0022, IN = 0x0023, PUTSP = 0x0024, HALT = 0x0025,
			KBSR = 0xFE00, KBDR = 0xFE02, DSR = 0xFE04, DDR = 0xFE06, MCR = 0xFFFE;
	int Reg[], mem[], exception;
	boolean display, keyboard;
	long clk;
	
	public CPU(){
		mem = new int[0x1<<16];
		Reg = new int[11];
		reinit();
	}
	
	public final void reinit(){
		open(Environment.getExternalStorageDirectory()+"/.LC3/ROM.obj");
		for(int i = 0; i<8; Reg[i++] = 0x0000);
		Reg[PC] = 0x3000;
		Reg[IR] = 0x0000;
		Reg[PSR] = 0x8002;
		exception = 0x80;
		clk = 0;
		display = keyboard = false;
	}
	
	public void excute_1(){
		clk++;
		if(((mem[KBSR]&0xC000)==0xC000)&&((Reg[PSR]&0x8000)>0)&&(((Reg[PSR]>>8)&0x0007)<0x0004)){
			Reg[6] = (Reg[6]-1)&0xFFFF;
			mem[Reg[6]] = Reg[PSR];
			Reg[6] = (Reg[6]-1)&0xFFFF;
			mem[Reg[6]] = Reg[PC];
			Reg[PC] = mem[0x0180];
			Reg[PSR] = (Reg[PSR]&0x78FF)+0x0400;
		}
		Reg[IR] = mem[Reg[PC]];
		keyboard = Reg[PC]==KBSR;
		Reg[PC] = (Reg[PC]+1)&0xFFFF;
		switch(Reg[IR]>>12){
		case 0x0:
			Reg[PC] = ((Reg[IR]>>9)&Reg[PSR]&0x0007)>0? (Reg[PC]+(((Reg[IR]&0x01FF)-0x0100)&0x01FF)-0x0100)&0xFFFF : Reg[PC];
			break;
		case 0x1:
			Reg[(Reg[IR]>>9)&0x0007] = (Reg[(Reg[IR]>>6)&0x0007]+((Reg[IR]&0x0020)>0? (((Reg[IR]&0x001F)-0x0010)&0x001F)-0x0010 : Reg[Reg[IR]&0x0007]))&0xFFFF;
			Reg[PSR] = (Reg[PSR]&0xFFF8)+((Reg[(Reg[IR]>>9)&0x0007]&0x8000)>0? 0x0004 : Reg[(Reg[IR]>>9)&0x0007]==0? 0x0002 : 0x0001);
			break;
		case 0x2:
			Reg[(Reg[IR]>>9)&0x0007] = mem[(Reg[PC]+(((Reg[IR]&0x01FF)-0x0100)&0x01FF)-0x0100)&0xFFFF];
			keyboard |= (((Reg[PC]+(((Reg[IR]&0x01FF)-0x0100)&0x01FF)-0x0100)&0xFFFF)==KBSR)||(((Reg[PC]+(((Reg[IR]&0x01FF)-0x0100)&0x01FF)-0x0100)&0xFFFF)==KBDR);
			Reg[PSR] = (Reg[PSR]&0xFFF8)+((Reg[(Reg[IR]>>9)&0x0007]&0x8000)>0? 0x0004 : Reg[(Reg[IR]>>9)&0x0007]==0? 0x0002 : 0x0001);
			break;
		case 0x3:
			mem[(Reg[PC]+(((Reg[IR]&0x01FF)-0x0100)&0x01FF)-0x0100)&0xFFFF] = Reg[(Reg[IR]>>9)&0x0007];
			display |= ((Reg[PC]+(((Reg[IR]&0x01FF)-0x0100)&0x01FF)-0x0100)&0xFFFF)==DDR;
			break;
		case 0x4:
			Reg[7] = Reg[PC];
			Reg[PC] = (Reg[IR]&0x0800)>0? (Reg[PC]+(((Reg[IR]&0x07FF)-0x0400)&0x07FF)-0x0400)&0xFFFF : Reg[(Reg[IR]>>6)&0x0007];
			break;
		case 0x5:
			Reg[(Reg[IR]>>9)&0x0007] = (Reg[(Reg[IR]>>6)&0x0007]&((Reg[IR]&0x0020)>0? (((Reg[IR]&0x001F)-0x0010)&0x001F)-0x0010 : Reg[Reg[IR]&0x0007]))&0xFFFF;
			Reg[PSR] = (Reg[PSR]&0xFFF8)+((Reg[(Reg[IR]>>9)&0x0007]&0x8000)>0? 0x0004 : Reg[(Reg[IR]>>9)&0x0007]==0? 0x0002 : 0x0001);
			break;
		case 0x6:
			Reg[(Reg[IR]>>9)&0x0007] = mem[(Reg[(Reg[IR]>>6)&0x0007]+(((Reg[IR]&0x003F)-0x0020)&0x003F)-0x0020)&0xFFFF];
			keyboard |= (((Reg[(Reg[IR]>>6)&0x0007]+(((Reg[IR]&0x003F)-0x0020)&0x003F)-0x0020)&0xFFFF)==KBSR)||(((Reg[(Reg[IR]>>6)&0x0007]+(((Reg[IR]&0x003F)-0x0020)&0x003F)-0x0020)&0xFFFF)==KBDR);
			Reg[PSR] = (Reg[PSR]&0xFFF8)+((Reg[(Reg[IR]>>9)&0x0007]&0x8000)>0? 0x0004 : Reg[(Reg[IR]>>9)&0x0007]==0? 0x0002 : 0x0001);
			break;
		case 0x7:
			mem[(Reg[(Reg[IR]>>6)&0x0007]+(((Reg[IR]&0x003F)-0x0020)&0x003F)-0x0020)&0xFFFF] = Reg[(Reg[IR]>>9)&0x0007];
			display |= ((Reg[(Reg[IR]>>6)&0x0007]+(((Reg[IR]&0x003F)-0x0020)&0x003F)-0x0020)&0xFFFF)==DDR;
			break;
		case 0x8:
			if((Reg[PSR]&0x8000)!=0){
				exception = 0x00;
				break;
			}
			Reg[PC] = mem[Reg[6]];
			Reg[6] = (Reg[6]+1)&0xFFFF;
			Reg[PSR] = mem[Reg[6]];
			Reg[6] = (Reg[6]+1)&0xFFFF;
			break;
		case 0x9:
			Reg[(Reg[IR]>>9)&0x0007] = (~Reg[(Reg[IR]>>6)&0x0007])&0xFFFF;
			Reg[PSR] = (Reg[PSR]&0xFFF8)+((Reg[(Reg[IR]>>9)&0x0007]&0x8000)>0? 0x0004 : Reg[(Reg[IR]>>9)&0x0007]==0? 0x0002 : 0x0001);
			break;
		case 0xA:
			Reg[(Reg[IR]>>9)&0x0007] = mem[mem[(Reg[PC]+(((Reg[IR]&0x01FF)-0x0100)&0x01FF)-0x0100)&0xFFFF]];
			keyboard |= ((mem[(Reg[PC]+(((Reg[IR]&0x01FF)-0x0100)&0x01FF)-0x0100)&0xFFFF])==KBSR)||((mem[(Reg[PC]+(((Reg[IR]&0x01FF)-0x0100)&0x01FF)-0x0100)&0xFFFF])==KBDR);
			Reg[PSR] = (Reg[PSR]&0xFFF8)+((Reg[(Reg[IR]>>9)&0x0007]&0x8000)>0? 0x0004 : Reg[(Reg[IR]>>9)&0x0007]==0? 0x0002 : 0x0001);
			break;
		case 0xB:
			mem[mem[(Reg[PC]+(((Reg[IR]&0x01FF)-0x0100)&0x01FF)-0x0100)&0xFFFF]] = Reg[(Reg[IR]>>9)&0x0007];
			display |= (mem[(Reg[PC]+(((Reg[IR]&0x01FF)-0x0100)&0x01FF)-0x0100)&0xFFFF])==DDR;
			break;
		case 0xC:
			Reg[PC] = Reg[(Reg[IR]>>6)&0x0007];
			break;
		case 0xD:
			exception = 0x01;
			break;
		case 0xE:
			Reg[(Reg[IR]>>9)&0x0007] = (Reg[PC]+(((Reg[IR]&0x01FF)-0x0100)&0x01FF)-0x0100)&0xFFFF;
			Reg[PSR] = (Reg[PSR]&0xFFF8)+((Reg[(Reg[IR]>>9)&0x0007]&0x8000)>0? 0x0004 : Reg[(Reg[IR]>>9)&0x0007]==0? 0x0002 : 0x0001);
			break;
		case 0xF:
			Reg[7] = Reg[PC];
			Reg[PC] = mem[Reg[IR]&0x00FF];
			break;
		}
	}
	
	public String disasm(int i){
		switch(mem[i]>>12){
		case 0x0:
			return (mem[i]&0x0E00)>0? String.format("BR%c%c%c x%04X", (mem[i]&0x0800)>0? 'N' : '\0', (mem[i]&0x0400)>0? 'Z' : '\0', (mem[i]&0x0200)>0? 'P' : '\0', (i+(((mem[i]&0x01FF)-0x0100)&0x01FF)-0x00FF)&0xFFFF) : "NOP";
		case 0x1:
			return String.format("ADD R%d, R%d, %s", (mem[i]>>9)&0x0007, (mem[i]>>6)&0x0007, (mem[i]&0x0020)>0? String.format("#%d", (((mem[i]&0x001F)-0x0010)&0x001F)-0x0010) : String.format("R%d", mem[i]&0x0007));
		case 0x2:
			return  String.format("LD R%d, x%04X", (mem[i]>>9)&0x0007, (i+(((mem[i]&0x01FF)-0x0100)&0x01FF)-0x00FF)&0xFFFF);
		case 0x3:
			return String.format("ST R%d, x%04X", (mem[i]>>9)&0x0007, (i+(((mem[i]&0x01FF)-0x0100)&0x01FF)-0x00FF)&0xFFFF);
		case 0x4:
			return (mem[i]&0x0800)>0? String.format("JSR x%04X", (i+(((mem[i]&0x07FF)-0x0400)&0x07FF)-0x03FF)&0xFFFF) : String.format("JSRR R%d", (mem[i]>>6)&0x0007);
		case 0x5:
			return String.format("AND R%d, R%d, %s", (mem[i]>>9)&0x0007, (mem[i]>>6)&0x0007, (mem[i]&0x0020)>0? String.format("#%d", (((mem[i]&0x001F)-0x0010)&0x001F)-0x0010) : String.format("R%d", mem[i]&0x0007));
		case 0x6:
			return String.format("LDR R%d, R%d, #%d", (mem[i]>>9)&0x0007, (mem[i]>>6)&0x0007, (((mem[i]&0x003F)-0x0020)&0x003F)-0x0020);
		case 0x7:
			return String.format("STR R%d, R%d, #%d", (mem[i]>>9)&0x0007, (mem[i]>>6)&0x0007, (((mem[i]&0x0003F)-0x0020)&0x003F)-0x0020);
		case 0x8:
			return "RTI";
		case 0x9:
			return String.format("NOT R%d, R%d", (mem[i]>>9)&0x0007, (mem[i]>>6)&0x0007);
		case 0xA:
			return String.format("LDI R%d, x%04X", (mem[i]>>9)&0x0007, (i+(((mem[i]&0x01FF)-0x0100)&0x01FF)-0x00FF)&0xFFFF);
		case 0xB:
			return String.format("STI R%d, x%04X", (mem[i]>>9)&0x0007, (i+(((mem[i]&0x01FF)-0x0100)&0x01FF)-0x00FF)&0xFFFF);
		case 0xC:
			return (mem[i]&0x01C0)==0x01C0? "RET" : String.format("JMP R%d", (mem[i]>>6)&0x0007);
		case 0xD:
			return "RESERVED";
		case 0xE:
			return String.format("LEA R%d, x%04X", (mem[i]>>9)&0x0007, (i+(((mem[i]&0x01FF)-0x0100)&0x01FF)-0x00FF)&0xFFFF);
		case 0xF:
			switch(mem[i]&0x00FF){
			case GETC:
				return "TRAP GETC";
			case OUT:
				return "TRAP OUT";
			case PUTS:
				return "TRAP PUTS";
			case IN:
				return "TRAP IN";
			case PUTSP:
				return "TRAP PUTSP";
			case HALT:
				return "TRAP HALT";
			default:
				return String.format("TRAP x%02X", mem[i]&0x00FF);
			}
		}
		return null;
	}
	
	public void open(String Path){
		try{
			FileInputStream fis = new FileInputStream(Path);
			byte b[] = new byte[fis.available()];
			fis.read(b);
			fis.close();
			Reg[PC] = ((((int)b[0])&0x00FF)<<8)+(b[1]&0x00FF);
			for(int i = 0; ++i<b.length>>1; mem[(Reg[PC]+i-1)&0xFFFF] = ((((int)b[i<<1])&0x00FF)<<8)+(b[(i<<1)+1]&0x00FF));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
}