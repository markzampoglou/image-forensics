function [out,f] = svmdecision(Xnew,svm_struct)
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
