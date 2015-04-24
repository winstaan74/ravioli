# Creating Resources just to a group #
can be done - provide a user with a nice ui for creating a new resource, and then give them the option of what groups to make this resource visible to.


The problem is securing any backing data / services so that they are restricted just to users of this group.

Still, a resource without any backing data might be useful - we could model notes / links to external websites / wiki-style pages used for collaboration in the group as resources - then they can be searched, shared, bookmarked just as any other resource.

To be really useful, need to have group-editing permissions on a resource too.

# Publishing Tables and Images just to a group #
  * registration
  * trust of our service
  * security of underlying data
    * depends critically on StateOfVospaceAndCommunity
    * and a little on StateOfSampTools

Maybe there's work-arounds - such as a download-from-vospace client, that runs on user's desktop?

We don't have a good solution to this yet. The problem is that the data needs to be on a public FTP server, so it is protected only by obscurity.

Solution

(i) let people do it as above, but don't put entry in public registry.
Advise them that this could expose their data.

(ii) Offer to host the data as well as the DSA service. Maybe we could do this for small tables, but not huge datasets.

(iii) Put data on sftp or https, and have user interrupted by a request for a password ? won't work well

(iv) Wait for VOSpace ...

(v) some magic with single use URLs?