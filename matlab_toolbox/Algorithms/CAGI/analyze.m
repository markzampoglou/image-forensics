% Copyright (C) 2016 C. Iakovidou
% Information Technologies Institute, Centre for Research and Technology Hellas
% 6th Km Harilaou-Thermis, Thessaloniki 57001, Greece


function [Result]=analyze(filenameIm)

    [ Result_CAGI,Result_Inv_CAGI ] = CAGI( filenameIm );
    
    % Right now we are returning CAGI; To get inv_CAGI modify the following
    % assignment: Result=Result_Inv_CAGI;
    Result=Result_CAGI;

end



