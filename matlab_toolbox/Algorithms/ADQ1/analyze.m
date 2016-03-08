function [OutputMap, Feature_Vector, coeffArray] = analyze( imPath )
    try
        %try_catch is used instead of simply checking the extension 
        %because often jpeg files have a wrong extension
        %evalc is used to suppress output when the file is not jpeg        
        [~,im] = evalc('jpeg_read(imPath);');
    catch
        im=CleanUpImage(imPath);
    end
    [OutputMap, Feature_Vector, coeffArray] = Extract_Features(im);
end

