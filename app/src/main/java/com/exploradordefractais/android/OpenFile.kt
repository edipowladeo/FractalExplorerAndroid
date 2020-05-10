package com.exploradordefractais.android

import android.content.Context

//fun LerArquivo (FileName: String)=  MyApplication.appContext.getAssets().open(FileName).bufferedReader().use{   it.readText()      }

fun LerArquivo (FileName: String)
        = MainApplication.applicationContext().getAssets()
            .open(FileName).bufferedReader().use{it.readText()}

fun LerArquivo (context: Context, FileName: String)
        = context.getAssets().open(FileName).bufferedReader().use{   it.readText()      }
