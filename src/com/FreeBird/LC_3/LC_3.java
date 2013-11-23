package com.FreeBird.LC_3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

public class LC_3 extends Activity{

	final int STOP = 0x101, PRINT = 0x102, PRINTOUT = 0x103, INPUT = 0x104, freshTime = 100, CC = 11;
	int top;
	boolean tinput;
	CPU cpu;
	LinearLayout dump;
	TextView Regs[], tvp, Output;
	Set breakpoints;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lc_3);
		TabHost tabHost = (TabHost)findViewById(R.id.tabhost);
		tabHost.setup();
		LayoutInflater.from(this).inflate(R.layout.simulator, tabHost.getTabContentView());
		LayoutInflater.from(this).inflate(R.layout.console, tabHost.getTabContentView());
		tabHost.addTab(tabHost.newTabSpec("tab01").setIndicator(getString(R.string.Simulator)).setContent(R.id.Sim));
		tabHost.addTab(tabHost.newTabSpec("tab02").setIndicator(getString(R.string.Terminal)).setContent(R.id.Con));
		final File dir = new File(Environment.getExternalStorageDirectory(), ".LC3"), f = new File(dir, "ROM.obj");
		if(!f.exists()){
			try{
				if(!dir.exists())
					dir.mkdir();
				f.createNewFile();
				InputStream is = getAssets().open("ROM.obj");
				FileOutputStream fos = new FileOutputStream(f);
				byte b[] = new byte[is.available()];
				is.read(b);
				is.close();
				fos.write(b);
				fos.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		cpu = new CPU();
		Output = (TextView)findViewById(R.id.Output);
		Output.setMovementMethod(ScrollingMovementMethod.getInstance()) ;
		breakpoints = new HashSet();
		Regs = new TextView[12];
		Regs[0] = (TextView)findViewById(R.id.textView2);
		Regs[1] = (TextView)findViewById(R.id.textView8);
		Regs[2] = (TextView)findViewById(R.id.textView14);
		Regs[3] = (TextView)findViewById(R.id.textView20);
		Regs[4] = (TextView)findViewById(R.id.textView4);
		Regs[5] = (TextView)findViewById(R.id.textView10);
		Regs[6] = (TextView)findViewById(R.id.textView16);
		Regs[7] = (TextView)findViewById(R.id.textView22);
		Regs[CPU.PC] = (TextView)findViewById(R.id.textView6);
		Regs[CPU.IR] = (TextView)findViewById(R.id.textView12);
		Regs[CPU.PSR] = (TextView)findViewById(R.id.textView18);
		Regs[CC] = (TextView)findViewById(R.id.textView24);
		dump = (LinearLayout)findViewById(R.id.mem);
		Output.setMovementMethod(ScrollingMovementMethod.getInstance()) ;
		top = cpu.Reg[CPU.PC];
		for(int i = 0; i<Regs.length; i++){
			final int fi = i;
			Regs[i].setOnClickListener(new OnClickListener(){
				public void onClick(View v){
					final Dialog dialog = new Dialog(LC_3.this);
					dialog.setContentView(R.layout.regs);
					dialog.show();
					final ImageButton change = (ImageButton)dialog.findViewById(R.id.change);
					switch(fi){
					case 0:
					case 1:
					case 2:
					case 3:
					case 4:
					case 5:
					case 6:
					case 7:
						dialog.setTitle("R"+fi);
						change.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v){
								final String str = ((EditText)dialog.findViewById(R.id.setValue)).getText().toString().toUpperCase();
								if(str.length()>0){
									cpu.Reg[fi] = 0x0000;
									for(int j = 0; j<str.length(); cpu.Reg[fi] = Hex(str.charAt(j++))+(cpu.Reg[fi]<<4));
									Regs[fi].setText(String.format("x%04X", cpu.Reg[fi]));
								}
								dialog.dismiss();
							}
						});
						break;
					case CPU.PC:
						dialog.setTitle("PC");
						change.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v){
								final String str = ((EditText)dialog.findViewById(R.id.setValue)).getText().toString().toUpperCase();
								if(str.length()>0){
									cpu.Reg[fi] = 0x0000;
									for(int j = 0; j<str.length(); cpu.Reg[fi] = Hex(str.charAt(j++))+(cpu.Reg[fi]<<4));
									Regs[fi].setText(String.format("x%04X", cpu.Reg[fi]));
									RefreshDump();
								}
								dialog.dismiss();
							}
						});
						break;
					case CPU.IR:
						dialog.setTitle("IR");
						change.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v){
								final String str = ((EditText)dialog.findViewById(R.id.setValue)).getText().toString().toUpperCase();
								if(str.length()>0){
									cpu.Reg[fi] = 0x0000;
									for(int j = 0; j<str.length(); cpu.Reg[fi] = Hex(str.charAt(j++))+(cpu.Reg[fi]<<4));
									Regs[fi].setText(String.format("x%04X", cpu.Reg[fi]));
								}
								dialog.dismiss();
							}
						});
						break;
					case CPU.PSR:
						dialog.setTitle("PSR");
						change.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v){
								final String str = ((EditText)dialog.findViewById(R.id.setValue)).getText().toString().toUpperCase();
								if(str.length()>0){
									cpu.Reg[fi] = 0x0000;
									for(int j = 0; j<str.length(); cpu.Reg[fi] = Hex(str.charAt(j++))+(cpu.Reg[fi]<<4));
									Regs[fi].setText(String.format("x%04X", cpu.Reg[fi]));
									switch(cpu.Reg[CPU.PSR]&0x0007){
									case 0x0001:
										Regs[CC].setText("P");
										break;
									case 0x0002:
										Regs[CC].setText("Z");
										break;
									case 0x0004:
										Regs[CC].setText("N");
										break;
									}
								}
								dialog.dismiss();
							}
						});
						break;
					case CC:
						dialog.setTitle("CC");
						change.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v){
								final String str = ((EditText)dialog.findViewById(R.id.setValue)).getText().toString().toUpperCase();
								if(str.length()>0){
									Regs[CC].setText(str);
									switch(str.charAt(0)){
									case 'N':
										cpu.Reg[CPU.PSR] = (cpu.Reg[CPU.PSR]&0xFFF8)+0x0004;
										break;
									case 'P':
										cpu.Reg[CPU.PSR] = (cpu.Reg[CPU.PSR]&0xFFF8)+0x0001;
										break;
									case 'Z':
										cpu.Reg[CPU.PSR] = (cpu.Reg[CPU.PSR]&0xFFF8)+0x0002;
										break;
									}
								}
								dialog.dismiss();
							}
						});
						break;
					}
				}
			});
		}
		RefreshDump();
		((ImageButton)findViewById(R.id.Jump)).setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				final Dialog dialog = new Dialog(LC_3.this);
				dialog.setContentView(R.layout.jump);
				dialog.setTitle(R.string.JumpTitle);
				dialog.show();
				((ImageButton)dialog.findViewById(R.id.jmp)).setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v){
						final String str = ((EditText)dialog.findViewById(R.id.jmpto)).getText().toString().toUpperCase();
						if(str.length()>0){
							int add = 0x0000;
							for(int j = 0; j<str.length(); add = (add<<4)+Hex(str.charAt(j++)));
							top = Math.min(add, 0xFFC0);
							RefreshDump();
						}
						dialog.dismiss();
					}
				});
			}
		});
		final Handler handler = new Handler(){
			StringBuffer output = new StringBuffer();
			@Override
			public void handleMessage(Message msg){
				switch(msg.what){
				case STOP:
					((ImageButton)findViewById(R.id.Start)).setBackgroundResource(R.drawable.play);
					((TextView)findViewById(R.id.textView26)).setText(cpu.clk+getString(R.string.Count));
					((TextView)findViewById(R.id.textView27)).setText(R.string.Idle);
					for(int i = -1; ++i<cpu.Reg.length; Regs[i].setText(String.format("x%04X", cpu.Reg[i])));
					switch(cpu.Reg[CPU.PSR]&0x0007){
					case 0x0001:
						Regs[CC].setText("P");
						break;
					case 0x0002:
						Regs[CC].setText("Z");
						break;
					case 0x0004:
						Regs[CC].setText("N");
						break;
					}
					RefreshDump();
					((ImageButton)findViewById(R.id.Open)).setEnabled(true);
					break;
				case PRINT:
					output.append((char)(cpu.mem[CPU.DDR]&0x00FF)).append((char)((cpu.mem[CPU.DDR]>>8)&0x00FF));
					cpu.mem[CPU.DSR] |= 0x8000;
					break;
				case INPUT:
					((ImageButton)findViewById(R.id.Input)).setBackgroundResource(R.drawable.end);
					((ImageButton)findViewById(R.id.Input)).setEnabled(true);
					break;
				case PRINTOUT:
					if(output.length()>0){
						Output.append(output.toString());
						output = new StringBuffer();
					}
					break;
				}
				super.handleMessage(msg);
			}
		};
		handler.postDelayed(new Runnable(){
			@Override
			public void run(){
				handler.sendMessage(handler.obtainMessage(PRINTOUT));
				handler.postDelayed(this, freshTime);
			}
		}, freshTime);
		((ImageButton)findViewById(R.id.Start)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				cpu.mem[CPU.MCR] = (cpu.mem[CPU.MCR]+0x8000)&0xFFFF;
				if((cpu.mem[CPU.MCR]&0x8000)>0){
					((ImageButton)v).setBackgroundResource(R.drawable.suspend);
					((ImageButton)findViewById(R.id.Open)).setEnabled(false);
					((TextView)findViewById(R.id.textView27)).setText(R.string.Running);
					(new Thread(new Runnable(){
						@Override
						public void run(){
							do{
								cpu.excute_1();
								if(cpu.display){
									cpu.display = false;
									if((cpu.mem[CPU.DSR]&0x8000)>0){
										cpu.mem[CPU.DSR] &= 0x7FFF;
										handler.sendMessage(handler.obtainMessage(PRINT));
									}
								}
								if(cpu.keyboard){
									cpu.keyboard = false;
									cpu.mem[CPU.KBSR] &= 0x7FFF;
								}
							}while(((cpu.mem[CPU.MCR]&0x8000)>0)&&(!breakpoints.contains(cpu.Reg[CPU.PC])));
							cpu.mem[CPU.MCR] &= 0x7FFF;
							handler.sendMessage(handler.obtainMessage(STOP));
						}
					})).start();
				}
				else{
					((ImageButton)v).setBackgroundResource(R.drawable.play);
					((ImageButton)findViewById(R.id.Open)).setEnabled(true);
					((TextView)findViewById(R.id.textView26)).setText(cpu.clk+getString(R.string.Count));
					((TextView)findViewById(R.id.textView27)).setText(R.string.Idle);
					for(int i = -1; ++i<cpu.Reg.length; Regs[i].setText(String.format("x%04X", cpu.Reg[i])));
					switch(cpu.Reg[CPU.PSR]&0x0007){
					case 0x0001:
						Regs[CC].setText("P");
						break;
					case 0x0002:
						Regs[CC].setText("Z");
						break;
					case 0x0004:
						Regs[CC].setText("N");
						break;
					}
					RefreshDump();
				}	
			};
		});
		((ImageButton)findViewById(R.id.Refresh)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				tinput = false;
				cpu.mem[CPU.MCR] &= 0xEFFF;
				cpu.reinit();
				breakpoints.clear();
				((ImageButton)findViewById(R.id.Start)).setBackgroundResource(R.drawable.play);
				((TextView)findViewById(R.id.textView26)).setText(R.string.Count0);
				((TextView)findViewById(R.id.textView27)).setText(R.string.Idle);
				for(int i = -1; ++i<cpu.Reg.length; Regs[i].setText(String.format("x%04X", cpu.Reg[i])));
				switch(cpu.Reg[CPU.PSR]&0x0007){
				case 0x0001:
					Regs[CC].setText("P");
					break;
				case 0x0002:
					Regs[CC].setText("Z");
					break;
				case 0x0004:
					Regs[CC].setText("N");
					break;
				}
				Output.setText("");
				top = 0x3000;
				RefreshDump();
				((ImageButton)findViewById(R.id.Open)).setEnabled(true);
			}
		});
		((ImageButton)findViewById(R.id.Clear)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				Output.setText("");
			}
		});
		((ImageButton)findViewById(R.id.Open)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				startActivityForResult((new Intent()).setType("File/*").setAction(Intent.ACTION_GET_CONTENT), 1);
			}
		});
		((ImageButton)findViewById(R.id.Input)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				final Dialog dialog = new Dialog(LC_3.this);
				dialog.setContentView(R.layout.input);
				dialog.setTitle(R.string.InputBuffer);
				dialog.show();
				((ImageButton)dialog.findViewById(R.id.Insert)).setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v){
						final String str = ((EditText)dialog.findViewById(R.id.Text)).getText().toString();
						if(str.length()>0){
							((ImageButton)v).setBackgroundResource(R.drawable.spec1);
							((ImageButton)v).setEnabled(false);
							(new Thread(new Runnable(){
								@Override
								public void run(){
									tinput = true;
									for(int i = 0; tinput&&(i<str.length()); i++){
										while(((cpu.mem[CPU.KBSR]&0x8000)>0)&&tinput);
										if(tinput){
											cpu.mem[CPU.KBSR] |= 0x8000;
											cpu.mem[CPU.KBDR] = str.charAt(i);
										}
									}
									handler.sendMessage(handler.obtainMessage(INPUT));
								}
							})).start();
						}
						dialog.dismiss();
					}
				});
			}
		});
		((ImageButton)findViewById(R.id.Info)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				final Dialog dialog = new Dialog(LC_3.this);
				dialog.setContentView(R.layout.info);
				dialog.setTitle(R.string.HelpTitle);
				dialog.show();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(resultCode==RESULT_OK)
			switch(requestCode){
			case 1:
				cpu.open(data.getData().toString().split("file:")[1]);
				((TextView)findViewById(R.id.textView26)).setText(cpu.clk+getString(R.string.Count));
				((TextView)findViewById(R.id.textView27)).setText(R.string.Idle);
				Regs[CPU.PC].setText(String.format("x%04X", cpu.Reg[CPU.PC]));
				top = Math.min(0xFFC0, cpu.Reg[CPU.PC]);
				RefreshDump();
			}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onBackPressed(){
		final AlertDialog builder = new AlertDialog.Builder(this).create();
		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle(R.string.app_name);
		builder.setMessage(getString(R.string.Quit));
		builder.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.No), new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				builder.dismiss();
			}
		});
		builder.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.Yes), new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				finish();
			}
		});
		builder.show();
	}

	int Hex(char ch){
		return (ch>='0')&&(ch<='F')&&((ch<='9')||(ch>='A'))? ch-'0'-(ch>>6)*7 : 0;
	}
	
	public final void RefreshDump(){
		dump.removeAllViews();
		for(int i = 0; i<0x0040; i++){
			final int addr = (top+i)&0xFFFF;
			LinearLayout ll = new LinearLayout(this);
			ll.setOrientation(LinearLayout.HORIZONTAL);
			final TextView tv = new TextView(this),
						   tvd = new TextView(this);
			if(addr==cpu.Reg[CPU.PC]){
				tv.setText("->\t");
				tvp = tv;
			}
			else
				tv.setText("\t");
			tv.setTextColor(Color.BLUE);
			tv.setBackgroundColor(breakpoints.contains(addr)? Color.RED : Color.WHITE);
			ll.addView(tv);
			tvd.setText(String.format("x%04X\t\tx%04X\t\t%s", addr, cpu.mem[addr], cpu.disasm(addr)));
			tvd.setTypeface(Typeface.MONOSPACE);
			ll.addView(tvd);
			ll.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					final Dialog dialog = new Dialog(LC_3.this);
					dialog.setContentView(R.layout.mem);
					dialog.setTitle(String.format("x%04X", addr));
					dialog.show();
					final Button b = (Button)dialog.findViewById(R.id.BreakPoint);
					if(breakpoints.contains(addr))
						b.setText(R.string.DelBP);
					b.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v){
							if(breakpoints.contains(addr)){
								breakpoints.remove(addr);
								((Button)v).setText(R.string.SetBP);
								tv.setBackgroundColor(Color.WHITE);
							}
							else{
								breakpoints.add(addr);
								((Button)v).setText(R.string.DelBP);
								tv.setBackgroundColor(Color.RED);
							}
							dialog.dismiss();
						}
					});
					((Button)dialog.findViewById(R.id.setPC)).setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v){
							if((top<=cpu.Reg[CPU.PC])&&(cpu.Reg[CPU.PC]<top+0x0040))
								tvp.setText("\t");
							cpu.Reg[CPU.PC] = addr;
							Regs[CPU.PC].setText(String.format("x%04X", cpu.Reg[CPU.PC]));
							tv.setText("->\t");
							tvp = tv;
							dialog.dismiss();
						}
					});
					((ImageButton)dialog.findViewById(R.id.change)).setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v){
							final String str = ((EditText)dialog.findViewById(R.id.setValue)).getText().toString().toUpperCase();
							if(str.length()>0){
								cpu.mem[addr] = 0x0000;
								for(int j = 0; j<str.length(); cpu.mem[addr] = Hex(str.charAt(j++))+(cpu.mem[addr]<<4));
								cpu.mem[addr] &= 0xFFFF;
								tvd.setText(String.format("x%04X\t\tx%04X\t\t%s", addr, cpu.mem[addr], cpu.disasm(addr)));
							}
							dialog.dismiss();
						}
					});
				}
			});
			dump.addView(ll);
		}
	}
}