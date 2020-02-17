package com.nemesiss.dev.piaprobox.Service.DaggerModules;

import android.content.Context;
import com.nemesiss.dev.piaprobox.Service.GlobalErrorHandler.ParseContentErrorHandler;
import com.nemesiss.dev.piaprobox.Service.GlobalErrorHandler.ParseContentErrorToastHandler;
import dagger.Module;
import dagger.Provides;

@Module
public class ErrorHandlerModules {

    private Context context;

    public ErrorHandlerModules(Context context) {
        this.context = context;
    }

    @Provides
    public Context getCtx() {
        return this.context;
    }


    @Provides
    @ParseContentErrorToastHandler.ToastHandler
    public  ParseContentErrorHandler toast() {
        return new ParseContentErrorToastHandler(context);
    }
}
