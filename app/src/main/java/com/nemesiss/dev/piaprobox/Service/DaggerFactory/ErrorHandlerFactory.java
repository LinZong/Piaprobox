package com.nemesiss.dev.piaprobox.Service.DaggerFactory;


import com.nemesiss.dev.piaprobox.Service.DaggerModules.ErrorHandlerModules;
import com.nemesiss.dev.piaprobox.Service.GlobalErrorHandler.ParseContentErrorHandler;
import com.nemesiss.dev.piaprobox.Service.GlobalErrorHandler.ParseContentErrorToastHandler;
import dagger.Component;

@Component(modules = {ErrorHandlerModules.class})
public interface ErrorHandlerFactory {

    @ParseContentErrorToastHandler.ToastHandler
    ParseContentErrorHandler handler();
}
