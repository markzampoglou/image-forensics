function [OutputMap, Feature_Vector, coeffArray] = analyze( imPath )
    % Copyright (C) 2016 Markos Zampoglou
    % Information Technologies Institute, Centre for Research and Technology Hellas
    % 6th Km Harilaou-Thermis, Thessaloniki 57001, Greece
    
    try
        %try_catch is used instead of simply checking the extension
        %because often jpeg files have a wrong extension
        %evalc is used to suppress output when the file is not jpeg
        [~,im] = evalc('jpeg_read(imPath);');
    catch
        im=CleanUpImage(imPath);
    end
    [OutputMap, Feature_Vector, coeffArray] = detectDQ(im);
end

