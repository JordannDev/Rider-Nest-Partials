namespace PartialClasses.Named;

public partial class CustomerRepository
{
    public bool Exists(int id) => id > 0;
}
