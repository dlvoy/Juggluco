/*      This file is part of Juggluco, an Android app to receive and display         */
/*      glucose values from Freestyle Libre 2 and 3 sensors.                         */
/*                                                                                   */
/*      Copyright (C) 2021 Jaap Korthals Altes <jaapkorthalsaltes@gmail.com>         */
/*                                                                                   */
/*      Juggluco is free software: you can redistribute it and/or modify             */
/*      it under the terms of the GNU General Public License as published            */
/*      by the Free Software Foundation, either version 3 of the License, or         */
/*      (at your option) any later version.                                          */
/*                                                                                   */
/*      Juggluco is distributed in the hope that it will be useful, but              */
/*      WITHOUT ANY WARRANTY; without even the implied warranty of                   */
/*      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         */
/*      See the GNU General Public License for more details.                         */
/*                                                                                   */
/*      You should have received a copy of the GNU General Public License            */
/*      along with Juggluco. If not, see <https://www.gnu.org/licenses/>.            */
/*                                                                                   */
/*      Fri Jan 27 15:31:05 CET 2023                                                 */


package tk.glucodata;

import android.content.Context;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

public class util {
private	static DateFormat dformat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT,DateFormat.DEFAULT);
static public String timestring(long tim) {
		return dformat.format(new Date(tim));
		}
public static CheckBox getcheckbox(Context context, String label, boolean val) {
	var check=new CheckBox(context);
	check.setText(label);
	check.setChecked(val);
	return check;
	}
	public static CheckBox getcheckbox(Context context,int res, boolean val) {
		return getcheckbox(context, context.getString(res),val);
	}
public static TextView getlabel(Context act, String text) {
	TextView label=new TextView(act);
	label.setText(text);
	return label;
	}
public static TextView getlabel(Context act, int res) {
	return getlabel(act,act.getString(res));

	}
public static Button getbutton(Context act, String text) {
	Button label=new Button(act);
	label.setText(text);
	return label;
	}


public static Button getbutton(Context act, int res) {
	return getbutton(act,act.getString(res));

	}

}