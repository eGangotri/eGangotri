# eGangotri Trouble Shooting

# Errors Post Upload
Errors that happen after uplaoding has started and failed cannot be resolved programatically, 
Those have to be dealt with manually.

Currently we have 2 Types of Errors observed

(1) Invalid Bucket Name
Very Frequent:
<?xml version='1.0' encoding='UTF-8'?><Error><Code>InvalidBucketName</Code><Message>The specified bucket is not valid.</Message><Resource>Bucket names should be valid archive identifiers; try someting matching this regular expression: ^[a-zA-Z0-9][a-zA-Z0-9_.-]{4,100}$ (or, if you are making unusual identifiers, this user may lack the special permission to do so)</Resource><RequestId>23c9171f-10c9-47f5-aed2-12557f67cfce</RequestId></Error>

Basically the Page URL has been assigned a Invalid Identifier. Manually change it by tweaking a few chars
    Tweak the Page URL identifier a bit like add an _today String.
Reupload. Will work

(2) Bad Content Invalid PDF 
<?xml version='1.0' encoding='UTF-8'?><Error><Code>BadContent</Code><Message>Uploaded content is unacceptable.</Message><Resource>Syntax error detected in pdf data. You may be able to repair the pdf file with a repair tool, pdftk is one such tool.</Resource><RequestId>08ddae19-4e5a-44b9-a887-e00b467a415e</RequestId></Error>
Download PDFTk.
Repair the PDF using PDFTk.
Go Back and then Forward again. After a wait of few moments the Upload page will show again.
 Reuplad the Repaired PDF
 
 This error below can also be solved by following step 1
 <?xml version='1.0' encoding='UTF-8'?>
 <Error><Code>InternalError</Code><Message>We encountered an internal error. Please try again.</Message><Resource>Failed to get necessary short term bucket lock for highestwisdomtonyduff_202002, please try again</Resource><RequestId>099e2a17-4a18-4dae-815e-125a24b65b5f</RequestId></Error>