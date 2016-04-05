function [out,f] = svmdecision(Xnew,svm_struct)
    % This is a modification of the default SVM Matlab classification. The
    % code has been changed in order to return the distance from the
    % separating hyperplane alongside the class label.
    %
    % Edited by Markos Zampoglou, 2016, ITI-CERTH, Thessaloniki, Greece
    %
    %SVMDECISION Evaluates the SVM decision function
    
    %   Copyright 2004-2012 The MathWorks, Inc.
    
    
    sv = svm_struct.SupportVectors;
    alphaHat = svm_struct.Alpha;
    bias = svm_struct.Bias;
    kfun = svm_struct.KernelFunction;
    kfunargs = svm_struct.KernelFunctionArgs;
    
    f = (feval(kfun,sv,Xnew,kfunargs{:})'*alphaHat(:)) + bias;
    out = sign(f);
    % points on the boundary are assigned to class 1
    out(out==0) = 1;
